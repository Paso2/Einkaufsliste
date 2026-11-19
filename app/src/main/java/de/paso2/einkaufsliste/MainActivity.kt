package de.paso2.einkaufsliste

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.paso2.einkaufsliste.data.ShoppingListItemEntity
import de.paso2.einkaufsliste.ui.MainViewModel
import de.paso2.einkaufsliste.ui.screens.AddItemBottomSheet
import de.paso2.einkaufsliste.ui.screens.CatalogScreen
import de.paso2.einkaufsliste.ui.screens.CategoriesScreen
import de.paso2.einkaufsliste.ui.screens.DuplicateItemDialog
import de.paso2.einkaufsliste.ui.screens.EditItemDialog
import de.paso2.einkaufsliste.ui.screens.ListsBottomSheet
import de.paso2.einkaufsliste.ui.screens.ShoppingListScreen
import de.paso2.einkaufsliste.ui.screens.ThemeSettingsDialog
import de.paso2.einkaufsliste.ui.theme.EinkaufslisteTheme

private enum class ScreenRoute {
    SHOPPING_LIST,
    CATEGORIES,
    CATALOG
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            val lists by vm.lists.collectAsStateWithLifecycle()
            val activeList by vm.activeList.collectAsStateWithLifecycle()
            val activeListUiState by vm.activeListUiState.collectAsStateWithLifecycle()
            val categories by vm.categories.collectAsStateWithLifecycle()
            val products by vm.products.collectAsStateWithLifecycle()
            val frequentProducts by vm.frequentProducts.collectAsStateWithLifecycle()
            val duplicatePrompt by vm.duplicatePrompt.collectAsStateWithLifecycle()
            val showCategoryHeaders by vm.showCategoryHeaders.collectAsStateWithLifecycle()

            var currentRoute by remember { mutableStateOf(ScreenRoute.SHOPPING_LIST) }
            var showListsSheet by remember { mutableStateOf(false) }
            var showAddItemSheet by remember { mutableStateOf(false) }
            var showThemeDialog by remember { mutableStateOf(false) }
            var editingItem by remember { mutableStateOf<ShoppingListItemEntity?>(null) }

            BackHandler(enabled = currentRoute != ScreenRoute.SHOPPING_LIST) {
                currentRoute = ScreenRoute.SHOPPING_LIST
            }

            EinkaufslisteTheme(themeMode = themeMode) {
                when (currentRoute) {
                    ScreenRoute.SHOPPING_LIST -> {
                        ShoppingListScreen(
                            uiState = activeListUiState,
                            frequentProducts = frequentProducts,
                            onOpenListsSheet = { showListsSheet = true },
                            onOpenAddItem = { showAddItemSheet = true },
                            onToggleItem = { vm.toggleItemChecked(it) },
                            onEditItem = { editingItem = it },
                            onClearChecked = { vm.clearCheckedItems() },
                            onQuickAddFrequent = { vm.addProductToList(it, null, null, "") },
                            onOpenCategories = { currentRoute = ScreenRoute.CATEGORIES },
                            onOpenCatalog = { currentRoute = ScreenRoute.CATALOG },
                            onOpenThemeDialog = { showThemeDialog = true },
                            showCategoryHeaders = showCategoryHeaders,
                            onToggleCategoryHeaders = { vm.toggleCategoryHeaders() }
                        )
                    }

                    ScreenRoute.CATEGORIES -> {
                        CategoriesScreen(
                            categories = categories,
                            onBack = { currentRoute = ScreenRoute.SHOPPING_LIST },
                            onMoveCategory = { id, up -> vm.moveCategory(id, up) },
                            onAddCategory = { vm.addCategory(it) },
                            onRenameCategory = { cat, name -> vm.renameCategory(cat, name) },
                            onDeleteCategory = { vm.deleteCategory(it) }
                        )
                    }

                    ScreenRoute.CATALOG -> {
                        CatalogScreen(
                            products = products,
                            categories = categories,
                            onBack = { currentRoute = ScreenRoute.SHOPPING_LIST },
                            onAddProduct = { name, catId, unit -> vm.addCatalogProduct(name, catId, unit) },
                            onUpdateProduct = { p, name, catId, unit -> vm.updateCatalogProduct(p, name, catId, unit) },
                            onDeleteProduct = { vm.deleteCatalogProduct(it) }
                        )
                    }
                }

                if (showListsSheet) {
                    ListsBottomSheet(
                        lists = lists,
                        activeListId = activeList?.id,
                        frequentProducts = frequentProducts,
                        onSelectList = { vm.selectList(it) },
                        onCreateList = { name, initial -> vm.createList(name, initial) },
                        onRenameList = { list, name -> vm.renameList(list, name) },
                        onDeleteList = { vm.deleteList(it) },
                        onDismiss = { showListsSheet = false }
                    )
                }

                if (showAddItemSheet) {
                    AddItemBottomSheet(
                        products = products,
                        categories = categories,
                        frequentProducts = frequentProducts,
                        onAddProduct = { prod, qty, unit, note ->
                            vm.addProductToList(prod, qty, unit, note)
                        },
                        onCreateAndAddProduct = { name, catId, unit, qty, note ->
                            vm.createAndAddProduct(name, catId, unit, qty, note)
                        },
                        onDismiss = { showAddItemSheet = false }
                    )
                }

                duplicatePrompt?.let { prompt ->
                    DuplicateItemDialog(
                        state = prompt,
                        onMerge = { vm.confirmMergeDuplicate() },
                        onSeparate = { vm.confirmSeparateDuplicate() },
                        onDismiss = { vm.dismissDuplicatePrompt() }
                    )
                }

                editingItem?.let { item ->
                    EditItemDialog(
                        item = item,
                        onSave = { qty, unit, note ->
                            vm.updateListItem(item, qty, unit, note)
                            editingItem = null
                        },
                        onDelete = {
                            vm.deleteListItem(item)
                            editingItem = null
                        },
                        onDismiss = { editingItem = null }
                    )
                }

                if (showThemeDialog) {
                    ThemeSettingsDialog(
                        currentMode = themeMode,
                        onSelectMode = { vm.setThemeMode(it) },
                        onDismiss = { showThemeDialog = false }
                    )
                }
            }
        }
    }
}
