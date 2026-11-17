package de.paso2.einkaufsliste.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("name")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long?,
    val defaultUnit: String? = null,
    val purchaseCount: Int = 0,
    val lastPurchasedAt: Long? = null
)

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("listId"), Index("productId"), Index("categoryId")]
)
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val productId: Long?,
    val productName: String,
    val categoryId: Long?,
    val quantity: Double? = null,
    val unit: String? = null,
    val note: String = "",
    val isChecked: Boolean = false,
    val checkedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CatalogSeedDto(
    val categories: List<CategorySeedDto>,
    val products: List<ProductSeedDto>
)

@Serializable
data class CategorySeedDto(
    val name: String,
    val sortOrder: Int
)

@Serializable
data class ProductSeedDto(
    val name: String,
    val category: String,
    val defaultUnit: String? = null
)
