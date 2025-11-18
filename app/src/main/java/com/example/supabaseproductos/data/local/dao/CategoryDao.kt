package com.example.supabaseproductos.data.local.dao

import androidx.room.*
import com.example.supabaseproductos.data.local.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id AND isDeleted = 0")
    suspend fun getCategoryById(id: String): Category?
    
    @Query("SELECT * FROM categories WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedCategories(): List<Category>
    
    @Query("SELECT * FROM categories WHERE isDeleted = 1 AND isSynced = 0")
    suspend fun getDeletedUnsyncedCategories(): List<Category>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)
    
    @Update
    suspend fun update(category: Category)
    
    @Query("UPDATE categories SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE categories SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("DELETE FROM categories WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun cleanupSyncedDeletedItems()
    
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
