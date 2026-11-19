package de.paso2.einkaufsliste.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.paso2.einkaufsliste.data.ProductEntity
import de.paso2.einkaufsliste.data.ShoppingListItemEntity
import de.paso2.einkaufsliste.domain.QuantityMerger
import de.paso2.einkaufsliste.ui.ActiveListUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShoppingListScreen(
    uiState: ActiveListUiState,
    frequentProducts: List<ProductEntity>,
    onOpenListsSheet: () -> Unit,
    onOpenAddItem: () -> Unit,
    onToggleItem: (ShoppingListItemEntity) -> Unit,
    onEditItem: (ShoppingListItemEntity) -> Unit,
    onClearChecked: () -> Unit,
    onQuickAddFrequent: (ProductEntity) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    showCategoryHeaders: Boolean,
    onToggleCategoryHeaders: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Surface(
                        onClick = onOpenListsSheet,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.activeList?.name ?: "Einkaufsliste",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (uiState.totalOpenCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = uiState.totalOpenCount.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Liste wechseln",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleCategoryHeaders) {
                        Icon(
                            imageVector = if (showCategoryHeaders) {
                                Icons.Outlined.Category
                            } else {
                                Icons.AutoMirrored.Outlined.ViewList
                            },
                            contentDescription = if (showCategoryHeaders) {
                                "Kategorien ausblenden"
                            } else {
                                "Kategorien einblenden"
                            },
                            tint = if (showCategoryHeaders) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (showCategoryHeaders) "Kategorien ausblenden"
                                        else "Kategorien einblenden"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (showCategoryHeaders) Icons.AutoMirrored.Outlined.ViewList
                                        else Icons.Outlined.Category,
                                        null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleCategoryHeaders()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Kategorien & Reihenfolge") },
                                leadingIcon = { Icon(Icons.Outlined.Category, null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenCategories()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Produktkatalog") },
                                leadingIcon = { Icon(Icons.Outlined.Inventory2, null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenCatalog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Design / Dunkelmodus") },
                                leadingIcon = { Icon(Icons.Outlined.DarkMode, null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenThemeDialog()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddItem,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Artikel hinzufügen")
            }
        }
    ) { innerPadding ->
        val isEmpty = uiState.openGroups.isEmpty() && uiState.checkedItems.isEmpty()

        if (isEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Liste ist noch leer",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tippe auf + oder übernimm häufig gekaufte Artikel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (frequentProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Häufig gekauft",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        frequentProducts.take(8).forEach { product ->
                            AssistChip(
                                onClick = { onQuickAddFrequent(product) },
                                label = { Text(product.name) },
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
                }
            }
        } else {
            val haptics = LocalHapticFeedback.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp)
            ) {
                uiState.openGroups.forEachIndexed { groupIndex, group ->
                    if (showCategoryHeaders) {
                        item(key = "header_${group.categoryId ?: -1L}") {
                            Text(
                                text = group.categoryName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(
                                        top = if (groupIndex == 0) 4.dp else 14.dp,
                                        bottom = 2.dp,
                                        start = 8.dp
                                    )
                            )
                        }
                    }
                    items(
                        items = group.items,
                        key = { "item_${it.id}" }
                    ) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { onToggleItem(item) },
                            onEdit = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEditItem(item)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                if (uiState.checkedItems.isNotEmpty()) {
                    item(key = "checked_divider") {
                        Row(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GEKAUFT (${uiState.checkedItems.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = onClearChecked,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Leeren")
                            }
                        }
                    }
                    items(
                        items = uiState.checkedItems,
                        key = { "item_${it.id}" }
                    ) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { onToggleItem(item) },
                            onEdit = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEditItem(item)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                item(key = "usage_hint") {
                    Text(
                        text = "Tippen = gekauft  ·  Lange drücken = bearbeiten",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Compact single-line row: check circle · name · note (truncated) · quantity.
 * Tap toggles "bought", long press opens the edit dialog.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingListItemEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qtyText = QuantityMerger.formatQuantity(item.quantity, item.unit)
    val contentAlpha = if (item.isChecked) 0.5f else 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(onClick = onToggle, onLongClick = onEdit)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (item.isChecked) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Gekauft",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val noteColor = MaterialTheme.colorScheme.onSurfaceVariant
            val noteFontSize = MaterialTheme.typography.bodySmall.fontSize
            val titleDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            textDecoration = titleDecoration
                        )
                    ) {
                        append(item.productName)
                    }
                    if (item.note.isNotBlank()) {
                        withStyle(
                            SpanStyle(
                                color = noteColor,
                                fontSize = noteFontSize,
                                fontWeight = FontWeight.Normal,
                                textDecoration = titleDecoration
                            )
                        ) {
                            append("  ·  ")
                            append(item.note)
                        }
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (qtyText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = qtyText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 40.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
