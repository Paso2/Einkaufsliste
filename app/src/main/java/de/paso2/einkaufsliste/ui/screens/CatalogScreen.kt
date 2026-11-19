package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.data.CategoryEntity
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.ui.components.CategoryDropdown
import de.paso2.einkaufsliste.ui.components.UnitDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onAddProduct: (String, Long?, String?) -> Unit,
    onUpdateProduct: (ProductEntity, String, Long?, String?) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit
) {
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val filtered = remember(query, products, categoryMap) {
        val q = query.trim()
        if (q.isEmpty()) products
        else products.filter { p ->
            p.name.contains(q, ignoreCase = true) ||
                (p.categoryId?.let { categoryMap[it]?.name }?.contains(q, ignoreCase = true) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produktkatalog (${products.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Produkt hinzufügen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Produkt oder Kategorie suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Leeren")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp)
            ) {
                items(filtered, key = { it.id }) { prod ->
                    val catName = prod.categoryId?.let { categoryMap[it]?.name } ?: "Sonstiges"
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingProduct = prod }
                                .padding(start = 8.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
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
                                    text = buildString {
                                        append(catName)
                                        if (!prod.defaultUnit.isNullOrBlank()) {
                                            append(" · ${prod.defaultUnit}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { deletingProduct = prod },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
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

    if (showAddDialog) {
        ProductEditorDialog(
            title = "Neues Produkt",
            initialName = "",
            initialCategoryId = categories.firstOrNull()?.id,
            initialUnit = "Stk",
            categories = categories,
            onConfirm = { name, catId, unit ->
                onAddProduct(name, catId, unit)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingProduct?.let { prod ->
        ProductEditorDialog(
            title = "Produkt bearbeiten",
            initialName = prod.name,
            initialCategoryId = prod.categoryId ?: categories.firstOrNull()?.id,
            initialUnit = prod.defaultUnit,
            categories = categories,
            onConfirm = { name, catId, unit ->
                onUpdateProduct(prod, name, catId, unit)
                editingProduct = null
            },
            onDismiss = { editingProduct = null }
        )
    }

    deletingProduct?.let { prod ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("„${prod.name}“ löschen?") },
            text = { Text("Das Produkt wird dauerhaft aus dem Katalog entfernt.") },
            confirmButton = {
                Button(onClick = {
                    onDeleteProduct(prod)
                    deletingProduct = null
                }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun ProductEditorDialog(
    title: String,
    initialName: String,
    initialCategoryId: Long?,
    initialUnit: String?,
    categories: List<CategoryEntity>,
    onConfirm: (String, Long?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedCatId by remember { mutableStateOf(initialCategoryId) }
    var selectedUnit by remember { mutableStateOf(initialUnit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Produktname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCatId,
                    onSelectCategory = { selectedCatId = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                UnitDropdown(
                    selectedUnit = selectedUnit,
                    onSelectUnit = { selectedUnit = it },
                    label = "Standard-Einheit (optional)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedCatId, selectedUnit) },
                enabled = name.isNotBlank()
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
