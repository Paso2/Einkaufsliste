package de.paso2.einkaufsliste.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val seedJson = Json { ignoreUnknownKeys = true }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "einkaufsliste.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedDatabase(context, getInstance(context))
                        }
                    }
                })
                .build()
        }

        private suspend fun seedDatabase(context: Context, db: AppDatabase) {
            val jsonString = context.assets.open("catalog_seed.json")
                .bufferedReader()
                .use { it.readText() }
            val seed = seedJson.decodeFromString<CatalogSeedDto>(jsonString)

            db.withTransaction {

            val categoryDao = db.categoryDao()
            val productDao = db.productDao()
            val listDao = db.shoppingListDao()

            val categoryMap = mutableMapOf<String, Long>()
            seed.categories.forEach { catDto ->
                val id = categoryDao.insert(
                    CategoryEntity(
                        name = catDto.name,
                        sortOrder = catDto.sortOrder
                    )
                )
                categoryMap[catDto.name] = id
            }

            val fallbackCategoryId = categoryMap["Sonstiges"]
            val productEntities = seed.products.map { prodDto ->
                ProductEntity(
                    name = prodDto.name,
                    categoryId = categoryMap[prodDto.category] ?: fallbackCategoryId,
                    defaultUnit = prodDto.defaultUnit
                )
            }
            productDao.insertAll(productEntities)

            if (listDao.getAllOnce().isEmpty()) {
                listDao.insert(ShoppingListEntity(name = "Einkaufsliste"))
            }
            }
        }
    }
}
