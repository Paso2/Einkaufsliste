package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.data.CategoryEntity
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.ui.components.CategoryDropdown
import de.paso2.einkaufsliste.ui.components.UnitDropdown

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddItemBottomSheet(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    frequentProducts: List<ProductEntity>,
    onAddProduct: (ProductEntity, Double?, String?, String) -> Unit,
    onCreateAndAddProduct: (String, Long?, String?, Double?, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var creatingNewName by remember { mutableStateOf<String?>(null) }

    var quantityText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var newProductCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.id)
    }

    val filteredProducts = remember(searchQuery, products) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            products
        } else {
            products.filter { it.name.contains(q, ignoreCase = true) }
                .sortedWith(
                    compareByDescending<ProductEntity> { it.name.startsWith(q, ignoreCase = true) }
                        .thenByDescending { it.purchaseCount }
                        .thenBy { it.name }
                )
        }
    }

    val exactMatchExists = remember(searchQuery, products) {
        val q = searchQuery.trim()
        q.isNotEmpty() && products.any { it.name.equals(q, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            when {
                selectedProduct != null -> {
                    val prod = selectedProduct!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 24.dp)
                    ) {
                        ProductDetailsComposer(
                            title = prod.name,
                            subtitle = prod.categoryId?.let { categoryMap[it]?.name } ?: "Sonstiges",
                            quantityText = quantityText,
                            onQuantityChange = { quantityText = it },
                            selectedUnit = selectedUnit,
                            onUnitSelect = { selectedUnit = it },
                            noteText = noteText,
                            onNoteChange = { noteText = it },
                            confirmEnabled = true,
                            onCancel = {
                                selectedProduct = null
                                quantityText = ""
                                noteText = ""
                            },
                            onConfirm = {
                                val qty = quantityText.replace(',', '.').toDoubleOrNull()
                                val unit = if (qty != null) selectedUnit else null
                                onAddProduct(prod, qty, unit, noteText)
                                selectedProduct = null
                                quantityText = ""
                                noteText = ""
                                onDismiss()
                            }
                        )
                    }
                }

                creatingNewName != null -> {
                    val currentNewName = creatingNewName!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Neues Produkt anlegen",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = currentNewName,
                            onValueChange = { creatingNewName = it },
                            label = { Text("Produktname") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryDropdown(
                            categories = categories,
                            selectedCategoryId = newProductCategoryId,
                            onSelectCategory = { newProductCategoryId = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ProductDetailsComposer(
                            title = "",
                            subtitle = null,
                            quantityText = quantityText,
                            onQuantityChange = { quantityText = it },
                            selectedUnit = selectedUnit,
                            onUnitSelect = { selectedUnit = it },
                            noteText = noteText,
                            onNoteChange = { noteText = it },
                            confirmEnabled = currentNewName.isNotBlank(),
                            onCancel = {
                                creatingNewName = null
                                quantityText = ""
                                noteText = ""
                            },
                            onConfirm = {
                                val qty = quantityText.replace(',', '.').toDoubleOrNull()
                                onCreateAndAddProduct(
                                    currentNewName,
                                    newProductCategoryId,
                                    selectedUnit,
                                    qty,
                                    noteText
                                )
                                creatingNewName = null
                                quantityText = ""
                                noteText = ""
                                onDismiss()
                            }
                        )
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Produkt suchen oder neu anlegen...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Leeren")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (searchQuery.isBlank() && frequentProducts.isNotEmpty()) {
                            item(key = "frequent_section") {
                                Text(
                                    text = "HÄUFIG GEKAUFT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    frequentProducts.take(10).forEach { prod ->
                                        AssistChip(
                                            onClick = {
                                                onAddProduct(prod, null, null, "")
                                                onDismiss()
                                            },
                                            label = { Text(prod.name) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ALLE PRODUKTE (${products.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        if (searchQuery.isNotBlank() && !exactMatchExists) {
                            item(key = "create_new") {
                                val trimmed = searchQuery.trim()
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clickable {
                                            creatingNewName = trimmed
                                            selectedUnit = "Stk"
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "„$trimmed“ neu anlegen & hinzufügen",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        items(filteredProducts, key = { it.id }) { prod ->
                            val catName = prod.categoryId?.let { categoryMap[it]?.name } ?: "Sonstiges"
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProduct = prod
                                            selectedUnit = prod.defaultUnit ?: "Stk"
                                            quantityText = ""
                                            noteText = ""
                                        }
                                        .padding(start = 6.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onAddProduct(prod, null, null, "")
                                            onDismiss()
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Schnell hinzufügen",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsComposer(
    title: String,
    subtitle: String?,
    quantityText: String,
    onQuantityChange: (String) -> Unit,
    selectedUnit: String?,
    onUnitSelect: (String?) -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title.isNotEmpty()) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = onQuantityChange,
                label = { Text("Menge") },
                placeholder = { Text("z. B. 500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            UnitDropdown(
                selectedUnit = selectedUnit,
                onSelectUnit = onUnitSelect,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            label = { Text("Notiz (optional, z. B. Magerquark, Sorte)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Zurück")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled
            ) {
                Text("Hinzufügen")
            }
        }
    }
}
