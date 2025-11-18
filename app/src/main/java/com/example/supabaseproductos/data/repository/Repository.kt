package com.example.supabaseproductos.data.repository

import android.content.Context
import com.example.supabaseproductos.data.local.AppDatabase
import com.example.supabaseproductos.data.local.entities.Category
import com.example.supabaseproductos.data.local.entities.Product
import com.example.supabaseproductos.data.remote.SupabaseDataSource
import com.example.supabaseproductos.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class Repository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val categoryDao = database.categoryDao()
    private val productDao = database.productDao()
    private val supabaseDataSource = SupabaseDataSource()
    private val networkMonitor = NetworkMonitor(context)

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: String): Category? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: Category): Result<Category> {
        val categoryWithId = category.copy(
            id = if (category.id.isEmpty()) UUID.randomUUID().toString() else category.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return if (networkMonitor.isConnected()) {
            // Try to insert to Supabase first
            val result = supabaseDataSource.insertCategory(categoryWithId.copy(isSynced = true))
            if (result.isSuccess) {
                // Insert to local database with synced flag
                categoryDao.insert(categoryWithId.copy(isSynced = true))
                Result.success(categoryWithId)
            } else {
                // If failed, insert to local with unsynced flag
                categoryDao.insert(categoryWithId.copy(isSynced = false))
                Result.success(categoryWithId)
            }
        } else {
            // Insert to local database with unsynced flag
            categoryDao.insert(categoryWithId.copy(isSynced = false))
            Result.success(categoryWithId)
        }
    }

    suspend fun updateCategory(category: Category): Result<Category> {
        val updatedCategory = category.copy(
            updatedAt = System.currentTimeMillis()
        )

        return if (networkMonitor.isConnected()) {
            val result = supabaseDataSource.updateCategory(updatedCategory.copy(isSynced = true))
            if (result.isSuccess) {
                categoryDao.update(updatedCategory.copy(isSynced = true))
                Result.success(updatedCategory)
            } else {
                categoryDao.update(updatedCategory.copy(isSynced = false))
                Result.success(updatedCategory)
            }
        } else {
            categoryDao.update(updatedCategory.copy(isSynced = false))
            Result.success(updatedCategory)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> {
        return if (networkMonitor.isConnected()) {
            val result = supabaseDataSource.deleteCategory(id)
            if (result.isSuccess) {
                categoryDao.softDelete(id)
                categoryDao.markAsSynced(id)
                Result.success(Unit)
            } else {
                categoryDao.softDelete(id)
                Result.success(Unit)
            }
        } else {
            categoryDao.softDelete(id)
            Result.success(Unit)
        }
    }

    // Product operations
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = 
        productDao.getProductsByCategory(categoryId)

    suspend fun getProductById(id: String): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Result<Product> {
        val productWithId = product.copy(
            id = if (product.id.isEmpty()) UUID.randomUUID().toString() else product.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return if (networkMonitor.isConnected()) {
            val result = supabaseDataSource.insertProduct(productWithId.copy(isSynced = true))
            if (result.isSuccess) {
                productDao.insert(productWithId.copy(isSynced = true))
                Result.success(productWithId)
            } else {
                productDao.insert(productWithId.copy(isSynced = false))
                Result.success(productWithId)
            }
        } else {
            productDao.insert(productWithId.copy(isSynced = false))
            Result.success(productWithId)
        }
    }

    suspend fun updateProduct(product: Product): Result<Product> {
        val updatedProduct = product.copy(
            updatedAt = System.currentTimeMillis()
        )

        return if (networkMonitor.isConnected()) {
            val result = supabaseDataSource.updateProduct(updatedProduct.copy(isSynced = true))
            if (result.isSuccess) {
                productDao.update(updatedProduct.copy(isSynced = true))
                Result.success(updatedProduct)
            } else {
                productDao.update(updatedProduct.copy(isSynced = false))
                Result.success(updatedProduct)
            }
        } else {
            productDao.update(updatedProduct.copy(isSynced = false))
            Result.success(updatedProduct)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> {
        return if (networkMonitor.isConnected()) {
            val result = supabaseDataSource.deleteProduct(id)
            if (result.isSuccess) {
                productDao.softDelete(id)
                productDao.markAsSynced(id)
                Result.success(Unit)
            } else {
                productDao.softDelete(id)
                Result.success(Unit)
            }
        } else {
            productDao.softDelete(id)
            Result.success(Unit)
        }
    }

    // Synchronization
    suspend fun syncData(): Result<Unit> {
        if (!networkMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection"))
        }

        return try {
            // Sync unsynced categories
            val unsyncedCategories = categoryDao.getUnsyncedCategories()
            unsyncedCategories.forEach { category ->
                supabaseDataSource.insertCategory(category)
                categoryDao.markAsSynced(category.id)
            }

            // Sync deleted categories
            val deletedCategories = categoryDao.getDeletedUnsyncedCategories()
            deletedCategories.forEach { category ->
                supabaseDataSource.deleteCategory(category.id)
                categoryDao.markAsSynced(category.id)
            }

            // Sync unsynced products
            val unsyncedProducts = productDao.getUnsyncedProducts()
            unsyncedProducts.forEach { product ->
                supabaseDataSource.insertProduct(product)
                productDao.markAsSynced(product.id)
            }

            // Sync deleted products
            val deletedProducts = productDao.getDeletedUnsyncedProducts()
            deletedProducts.forEach { product ->
                supabaseDataSource.deleteProduct(product.id)
                productDao.markAsSynced(product.id)
            }

            // Fetch remote data
            val remoteCategories = supabaseDataSource.getCategories()
            if (remoteCategories.isSuccess) {
                val categories = remoteCategories.getOrNull()?.map { it.copy(isSynced = true) }
                categories?.let { categoryDao.insertAll(it) }
            }

            val remoteProducts = supabaseDataSource.getProducts()
            if (remoteProducts.isSuccess) {
                val products = remoteProducts.getOrNull()?.map { it.copy(isSynced = true) }
                products?.let { productDao.insertAll(it) }
            }

            // Cleanup synced deleted items
            categoryDao.cleanupSyncedDeletedItems()
            productDao.cleanupSyncedDeletedItems()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeNetworkConnectivity(): Flow<Boolean> = networkMonitor.observeConnectivity()
    
    fun isConnected(): Boolean = networkMonitor.isConnected()
}
