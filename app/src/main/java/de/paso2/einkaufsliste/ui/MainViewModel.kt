package de.paso2.einkaufsliste.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.paso2.einkaufsliste.EinkaufslisteApp
import de.paso2.einkaufsliste.data.CategoryEntity
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.data.ShoppingListEntity
import de.paso2.einkaufsliste.data.ShoppingListItemEntity
import de.paso2.einkaufsliste.data.ThemeMode
import de.paso2.einkaufsliste.domain.MergeResult
import de.paso2.einkaufsliste.domain.QuantityMerger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryGroup(
    val categoryId: Long?,
    val categoryName: String,
    val sortOrder: Int,
    val items: List<ShoppingListItemEntity>
)

data class ActiveListUiState(
    val activeList: ShoppingListEntity? = null,
    val openGroups: List<CategoryGroup> = emptyList(),
    val checkedItems: List<ShoppingListItemEntity> = emptyList(),
    val totalOpenCount: Int = 0
)

data class DuplicatePromptState(
    val existingItem: ShoppingListItemEntity,
    val product: ProductEntity,
    val newQuantity: Double?,
    val newUnit: String?,
    val newNote: String,
    val mergePreview: MergeResult
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EinkaufslisteApp
    private val db = app.database
    private val prefs = app.preferences

    private val categoryDao = db.categoryDao()
    private val productDao = db.productDao()
    private val listDao = db.shoppingListDao()
    private val itemDao = db.shoppingListItemDao()

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val showCategoryHeaders: StateFlow<Boolean> = prefs.showCategoryHeaders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleCategoryHeaders() {
        viewModelScope.launch { prefs.setShowCategoryHeaders(!showCategoryHeaders.value) }
    }

    val lists: StateFlow<List<ShoppingListEntity>> = listDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = productDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val frequentProducts: StateFlow<List<ProductEntity>> = combine(
        productDao.observeMostFrequent(15),
        products
    ) { frequent, all ->
        if (frequent.isNotEmpty()) {
            frequent
        } else {
            // Helpful starter suggestions before the user has checked off items
            val starterNames = listOf(
                "Milch", "Brot (Mischbrot)", "Eier", "Butter", "Banane",
                "Apfel", "Tomaten", "Salatgurke", "Käse", "Gouda", "Toilettenpapier"
            )
            all.filter { it.name in starterNames }.take(10)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeList: StateFlow<ShoppingListEntity?> = combine(
        lists,
        prefs.activeListId
    ) { allLists, savedId ->
        allLists.firstOrNull { it.id == savedId } ?: allLists.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val rawItemsForActiveList = activeList.flatMapLatest { list ->
        if (list == null) flowOf(emptyList()) else itemDao.observeForList(list.id)
    }

    val activeListUiState: StateFlow<ActiveListUiState> = combine(
        activeList,
        rawItemsForActiveList,
        categories
    ) { list, items, cats ->
        val catMap = cats.associateBy { it.id }
        val openItems = items.filter { !it.isChecked }
        val checkedItems = items.filter { it.isChecked }
            .sortedByDescending { it.checkedAt ?: 0L }

        val grouped = openItems.groupBy { it.categoryId }
            .map { (catId, groupItems) ->
                val cat = catId?.let { catMap[it] }
                CategoryGroup(
                    categoryId = cat?.id,
                    categoryName = cat?.name ?: "Sonstiges",
                    sortOrder = cat?.sortOrder ?: Int.MAX_VALUE,
                    items = groupItems.sortedBy { it.addedAt }
                )
            }
            .sortedWith(compareBy<CategoryGroup> { it.sortOrder }.thenBy { it.categoryName })

        ActiveListUiState(
            activeList = list,
            openGroups = grouped,
            checkedItems = checkedItems,
            totalOpenCount = openItems.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveListUiState())

    private val _duplicatePrompt = MutableStateFlow<DuplicatePromptState?>(null)
    val duplicatePrompt: StateFlow<DuplicatePromptState?> = _duplicatePrompt

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun selectList(listId: Long) {
        viewModelScope.launch { prefs.setActiveListId(listId) }
    }

    fun createList(name: String, initialProducts: List<ProductEntity> = emptyList()) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val newId = listDao.insert(ShoppingListEntity(name = trimmed))
            initialProducts.forEach { product ->
                itemDao.insert(
                    ShoppingListItemEntity(
                        listId = newId,
                        productId = product.id,
                        productName = product.name,
                        categoryId = product.categoryId,
                        quantity = null,
                        unit = product.defaultUnit,
                        note = ""
                    )
                )
            }
            prefs.setActiveListId(newId)
        }
    }

    fun renameList(list: ShoppingListEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            listDao.update(list.copy(name = trimmed))
        }
    }

    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            val currentLists = listDao.getAllOnce()
            if (currentLists.size <= 1) {
                // Keep at least one list, just clear it or rename? Better: create a fresh default if deleting last
                listDao.delete(list)
                val fallbackId = listDao.insert(ShoppingListEntity(name = "Einkaufsliste"))
                prefs.setActiveListId(fallbackId)
            } else {
                listDao.delete(list)
                if (activeList.value?.id == list.id) {
                    val next = currentLists.firstOrNull { it.id != list.id }
                    if (next != null) prefs.setActiveListId(next.id)
                }
            }
        }
    }

    fun addProductToList(
        product: ProductEntity,
        quantity: Double?,
        unit: String?,
        note: String
    ) {
        val listId = activeList.value?.id ?: return
        val cleanNote = note.trim()
        val cleanUnit = unit?.trim()?.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            val existingItems = itemDao.getForListOnce(listId)
            val existing = existingItems
                .filter {
                    (it.productId != null && it.productId == product.id) ||
                        it.productName.equals(product.name, ignoreCase = true)
                }
                .minByOrNull { it.isChecked }

            if (existing != null) {
                val mergePreview = QuantityMerger.merge(
                    existingQty = existing.quantity,
                    existingUnit = existing.unit,
                    existingNote = existing.note,
                    newQty = quantity,
                    newUnit = cleanUnit,
                    newNote = cleanNote
                )
                _duplicatePrompt.value = DuplicatePromptState(
                    existingItem = existing,
                    product = product,
                    newQuantity = quantity,
                    newUnit = cleanUnit,
                    newNote = cleanNote,
                    mergePreview = mergePreview
                )
            } else {
                itemDao.insert(
                    ShoppingListItemEntity(
                        listId = listId,
                        productId = product.id,
                        productName = product.name,
                        categoryId = product.categoryId,
                        quantity = quantity,
                        unit = cleanUnit,
                        note = cleanNote
                    )
                )
            }
        }
    }

    fun dismissDuplicatePrompt() {
        _duplicatePrompt.value = null
    }

    fun confirmMergeDuplicate() {
        val state = _duplicatePrompt.value ?: return
        _duplicatePrompt.value = null
        viewModelScope.launch {
            itemDao.update(
                state.existingItem.copy(
                    quantity = state.mergePreview.quantity,
                    unit = state.mergePreview.unit,
                    note = state.mergePreview.note,
                    isChecked = false,
                    checkedAt = null
                )
            )
        }
    }

    fun confirmSeparateDuplicate() {
        val state = _duplicatePrompt.value ?: return
        _duplicatePrompt.value = null
        viewModelScope.launch {
            itemDao.insert(
                ShoppingListItemEntity(
                    listId = state.existingItem.listId,
                    productId = state.product.id,
                    productName = state.product.name,
                    categoryId = state.product.categoryId,
                    quantity = state.newQuantity,
                    unit = state.newUnit,
                    note = state.newNote
                )
            )
        }
    }

    fun toggleItemChecked(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            val nowChecked = !item.isChecked
            itemDao.update(
                item.copy(
                    isChecked = nowChecked,
                    checkedAt = if (nowChecked) System.currentTimeMillis() else null
                )
            )
            if (nowChecked && item.productId != null) {
                productDao.incrementPurchaseCount(item.productId, System.currentTimeMillis())
            }
        }
    }

    fun updateListItem(
        item: ShoppingListItemEntity,
        quantity: Double?,
        unit: String?,
        note: String
    ) {
        viewModelScope.launch {
            itemDao.update(
                item.copy(
                    quantity = quantity,
                    unit = unit?.trim()?.takeIf { it.isNotEmpty() },
                    note = note.trim()
                )
            )
        }
    }

    fun deleteListItem(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    fun clearCheckedItems() {
        val listId = activeList.value?.id ?: return
        viewModelScope.launch {
            itemDao.deleteCheckedForList(listId)
        }
    }

    fun createAndAddProduct(
        name: String,
        categoryId: Long?,
        defaultUnit: String?,
        quantity: Double?,
        note: String
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val cleanUnit = defaultUnit?.trim()?.takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            val newProduct = ProductEntity(
                name = trimmed,
                categoryId = categoryId,
                defaultUnit = cleanUnit
            )
            val newId = productDao.insert(newProduct)
            addProductToList(
                product = newProduct.copy(id = newId),
                quantity = quantity,
                unit = cleanUnit,
                note = note
            )
        }
    }

    fun addCatalogProduct(name: String, categoryId: Long?, defaultUnit: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            productDao.insert(
                ProductEntity(
                    name = trimmed,
                    categoryId = categoryId,
                    defaultUnit = defaultUnit?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
        }
    }

    fun updateCatalogProduct(product: ProductEntity, name: String, categoryId: Long?, defaultUnit: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            productDao.update(
                product.copy(
                    name = trimmed,
                    categoryId = categoryId,
                    defaultUnit = defaultUnit?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
            itemDao.updateCategoryForProduct(product.id, categoryId)
        }
    }

    fun deleteCatalogProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.delete(product)
        }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val nextOrder = categoryDao.getMaxSortOrder() + 1
            categoryDao.insert(CategoryEntity(name = trimmed, sortOrder = nextOrder))
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            categoryDao.update(category.copy(name = trimmed))
        }
    }

    fun moveCategory(categoryId: Long, moveUp: Boolean) {
        viewModelScope.launch {
            val current = categoryDao.getAllOnce().toMutableList()
            val idx = current.indexOfFirst { it.id == categoryId }
            if (idx == -1) return@launch
            val targetIdx = if (moveUp) idx - 1 else idx + 1
            if (targetIdx !in current.indices) return@launch

            val item = current.removeAt(idx)
            current.add(targetIdx, item)
            val reindexed = current.mapIndexed { index, cat ->
                cat.copy(sortOrder = index)
            }
            categoryDao.updateAll(reindexed)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val all = categoryDao.getAllOnce()
            val fallback = all.firstOrNull { it.id != category.id && it.name == "Sonstiges" }
            productDao.reassignCategory(category.id, fallback?.id)
            itemDao.reassignCategory(category.id, fallback?.id)
            categoryDao.delete(category)
        }
    }
}
