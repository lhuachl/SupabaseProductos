package com.example.supabaseproductos.data.remote

import com.example.supabaseproductos.data.local.entities.Category
import com.example.supabaseproductos.data.local.entities.Product
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow

class SupabaseDataSource {
    private val client = SupabaseClientProvider.client

    // Category CRUD operations
    suspend fun insertCategory(category: Category): Result<Category> {
        return try {
            val result = client.from("categories")
                .insert(category)
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val result = client.from("categories")
                .select()
                .decodeList<Category>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Category> {
        return try {
            client.from("categories")
                .update(category) {
                    filter {
                        eq("id", category.id)
                    }
                }
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            client.from("categories")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Product CRUD operations
    suspend fun insertProduct(product: Product): Result<Product> {
        return try {
            client.from("products")
                .insert(product)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val result = client.from("products")
                .select()
                .decodeList<Product>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(categoryId: String): Result<List<Product>> {
        return try {
            val result = client.from("products")
                .select {
                    filter {
                        eq("categoryId", categoryId)
                    }
                }
                .decodeList<Product>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            client.from("products")
                .update(product) {
                    filter {
                        eq("id", product.id)
                    }
                }
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            client.from("products")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Realtime subscriptions
    fun subscribeToCategoryChanges(): Flow<PostgresAction> {
        return client.channel("categories-changes")
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "categories"
            }
    }

    fun subscribeToProductChanges(): Flow<PostgresAction> {
        return client.channel("products-changes")
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "products"
            }
    }
}
