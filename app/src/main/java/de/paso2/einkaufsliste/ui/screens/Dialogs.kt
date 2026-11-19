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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.BuildConfig
import de.paso2.einkaufsliste.data.ShoppingListItemEntity
import de.paso2.einkaufsliste.data.ThemeMode
import de.paso2.einkaufsliste.domain.QuantityMerger
import de.paso2.einkaufsliste.ui.DuplicatePromptState
import de.paso2.einkaufsliste.ui.components.UnitDropdown

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuplicateItemDialog(
    state: DuplicatePromptState,
    onMerge: () -> Unit,
    onSeparate: () -> Unit,
    onDismiss: () -> Unit
) {
    val existingQty = QuantityMerger.formatQuantity(state.existingItem.quantity, state.existingItem.unit)
    val newQty = QuantityMerger.formatQuantity(state.newQuantity, state.newUnit)
    val mergedQty = QuantityMerger.formatQuantity(state.mergePreview.quantity, state.mergePreview.unit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${state.product.name} bereits vorhanden") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.existingItem.isChecked) {
                    Text(
                        text = "Dieser Artikel steht bereits als „Gekauft“ unten auf der Liste.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Dieser Artikel steht bereits auf der Einkaufsliste. Möchtest du die Einträge zusammenführen?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Aktuell: ${existingQty.ifEmpty { "1x" }}" +
                                if (state.existingItem.note.isNotBlank()) " (${state.existingItem.note})" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Neu: ${newQty.ifEmpty { "1x" }}" +
                                if (state.newNote.isNotBlank()) " (${state.newNote})" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = "Zusammengeführt: ${mergedQty.ifEmpty { "1x" }}" +
                                if (state.mergePreview.note.isNotBlank()) " (${state.mergePreview.note})" else "",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
                OutlinedButton(onClick = onSeparate) {
                    Text("Separat")
                }
                Button(onClick = onMerge) {
                    Text("Zusammenführen")
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemDialog(
    item: ShoppingListItemEntity,
    onSave: (Double?, String?, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var qtyText by remember(item.id) {
        mutableStateOf(
            item.quantity?.let { QuantityMerger.formatQuantity(it, null) } ?: ""
        )
    }
    var selectedUnit by remember(item.id) { mutableStateOf(item.unit) }
    var noteText by remember(item.id) { mutableStateOf(item.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.productName) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Menge") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    UnitDropdown(
                        selectedUnit = selectedUnit,
                        onSelectUnit = { selectedUnit = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notiz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onDelete) {
                    Text("Entfernen", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
                Button(onClick = {
                    val parsedQty = qtyText.replace(',', '.').toDoubleOrNull()
                    onSave(parsedQty, if (parsedQty != null) selectedUnit else null, noteText)
                }) {
                    Text("Speichern")
                }
            }
        }
    )
}

@Composable
fun ThemeSettingsDialog(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Design & Info") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Erscheinungsbild",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onSelectMode(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fertig")
            }
        }
    )
}
