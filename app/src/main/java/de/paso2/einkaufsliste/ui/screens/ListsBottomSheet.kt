package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.data.ShoppingListEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListsBottomSheet(
    lists: List<ShoppingListEntity>,
    activeListId: Long?,
    frequentProducts: List<ProductEntity>,
    onSelectList: (Long) -> Unit,
    onCreateList: (String, List<ProductEntity>) -> Unit,
    onRenameList: (ShoppingListEntity, String) -> Unit,
    onDeleteList: (ShoppingListEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateDialog by remember { mutableStateOf(false) }
    var renamingList by remember { mutableStateOf<ShoppingListEntity?>(null) }
    var deletingList by remember { mutableStateOf<ShoppingListEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Einkaufslisten",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Neue Liste")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lists, key = { it.id }) { list ->
                    val isSelected = list.id == activeListId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectList(list.id)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Aktiv",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(
                                text = list.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { renamingList = list }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Umbenennen")
                            }
                            IconButton(onClick = { deletingList = list }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Löschen")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var listName by remember { mutableStateOf("") }
        var selectedIds by remember { mutableStateOf(setOf<Long>()) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Neue Einkaufsliste") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text("Name (z. B. Wocheneinkauf)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (frequentProducts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Häufig gekauft – direkt übernehmen:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            frequentProducts.take(10).forEach { product ->
                                val selected = product.id in selectedIds
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedIds = if (selected) {
                                            selectedIds - product.id
                                        } else {
                                            selectedIds + product.id
                                        }
                                    },
                                    label = { Text(product.name) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chosen = frequentProducts.filter { it.id in selectedIds }
                        onCreateList(listName.ifBlank { "Neue Liste" }, chosen)
                        showCreateDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    renamingList?.let { target ->
        var newName by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renamingList = null },
            title = { Text("Liste umbenennen") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Listenname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameList(target, newName)
                        renamingList = null
                    },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingList = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    deletingList?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingList = null },
            title = { Text("„${target.name}“ löschen?") },
            text = { Text("Alle Artikel in dieser Einkaufsliste werden entfernt.") },
            confirmButton = {
                Button(onClick = {
                    onDeleteList(target)
                    deletingList = null
                }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingList = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
