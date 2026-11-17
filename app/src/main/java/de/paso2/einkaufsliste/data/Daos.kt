package de.paso2.einkaufsliste.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllOnce(): List<CategoryEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM categories")
    suspend fun getMaxSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    @Update
    suspend fun updateAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE purchaseCount > 0 ORDER BY purchaseCount DESC, lastPurchasedAt DESC LIMIT :limit")
    fun observeMostFrequent(limit: Int = 15): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET purchaseCount = purchaseCount + 1, lastPurchasedAt = :timestamp WHERE id = :productId")
    suspend fun incrementPurchaseCount(productId: Long, timestamp: Long)

    @Query("UPDATE products SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long?)
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists ORDER BY createdAt ASC")
    suspend fun getAllOnce(): List<ShoppingListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingListEntity): Long

    @Update
    suspend fun update(list: ShoppingListEntity)

    @Delete
    suspend fun delete(list: ShoppingListEntity)
}

@Dao
interface ShoppingListItemDao {
    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId")
    fun observeForList(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId")
    suspend fun getForListOnce(listId: Long): List<ShoppingListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Update
    suspend fun update(item: ShoppingListItemEntity)

    @Delete
    suspend fun delete(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedForList(listId: Long)

    @Query("UPDATE shopping_list_items SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long?)

    @Query("UPDATE shopping_list_items SET categoryId = :newCategoryId WHERE productId = :productId")
    suspend fun updateCategoryForProduct(productId: Long, newCategoryId: Long?)
}
