package com.example.supabaseproductos.data.local.dao

import androidx.room.*
import com.example.supabaseproductos.data.local.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id AND isDeleted = 0")
    suspend fun getProductById(id: String): Product?
    
    @Query("SELECT * FROM products WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedProducts(): List<Product>
    
    @Query("SELECT * FROM products WHERE isDeleted = 1 AND isSynced = 0")
    suspend fun getDeletedUnsyncedProducts(): List<Product>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
    
    @Update
    suspend fun update(product: Product)
    
    @Query("UPDATE products SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("DELETE FROM products WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun cleanupSyncedDeletedItems()
    
    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
