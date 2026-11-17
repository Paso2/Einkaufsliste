package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onMoveCategory: (Long, Boolean) -> Unit,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (CategoryEntity, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategorien & Reihenfolge") },
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
                Icon(Icons.Default.Add, contentDescription = "Kategorie hinzufügen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Die Einkaufsliste wird exakt in dieser Reihenfolge sortiert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(categories, key = { _, cat -> cat.id }) { index, cat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onMoveCategory(cat.id, true) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Nach oben")
                            }
                            IconButton(
                                onClick = { onMoveCategory(cat.id, false) },
                                enabled = index < categories.lastIndex
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Nach unten")
                            }
                            IconButton(onClick = { editingCategory = cat }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Umbenennen")
                            }
                            IconButton(onClick = { deletingCategory = cat }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Löschen")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Neue Kategorie") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kategoriename") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCategory(name)
                        showAddDialog = false
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    editingCategory?.let { cat ->
        var name by remember(cat.id) { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Kategorie umbenennen") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameCategory(cat, name)
                        editingCategory = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) { Text("Abbrechen") }
            }
        )
    }

    deletingCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("„${cat.name}“ löschen?") },
            text = {
                Text("Produkte dieser Kategorie werden automatisch „Sonstiges“ zugewiesen.")
            },
            confirmButton = {
                Button(onClick = {
                    onDeleteCategory(cat)
                    deletingCategory = null
                }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text("Abbrechen") }
            }
        )
    }
}
