package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.data.CategoryEntity
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.domain.QuantityMerger

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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { prod ->
                    val catName = prod.categoryId?.let { categoryMap[it]?.name } ?: "Sonstiges"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingProduct = prod }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = buildString {
                                        append(catName)
                                        if (!prod.defaultUnit.isNullOrBlank()) {
                                            append(" • Standard: ${prod.defaultUnit}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteProduct(prod) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Produktname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Kategorie",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Standard-Einheit (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(QuantityMerger.standardUnits) { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = if (selectedUnit == unit) null else unit },
                            label = { Text(unit) }
                        )
                    }
                }
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
