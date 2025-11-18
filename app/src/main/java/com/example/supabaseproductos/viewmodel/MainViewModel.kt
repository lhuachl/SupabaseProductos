package com.example.supabaseproductos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseproductos.data.local.entities.Category
import com.example.supabaseproductos.data.local.entities.Product
import com.example.supabaseproductos.data.repository.Repository
import com.example.supabaseproductos.util.SoundManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}

data class AppState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val isConnected: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedCategory: Category? = null,
    val uiState: UiState = UiState.Idle
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val soundManager = SoundManager()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _notification = MutableSharedFlow<String>()
    val notification: SharedFlow<String> = _notification.asSharedFlow()

    init {
        observeCategories()
        observeProducts()
        observeNetworkConnectivity()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _appState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _appState.update { it.copy(products = products) }
            }
        }
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            repository.observeNetworkConnectivity().collect { isConnected ->
                val previousState = _appState.value.isConnected
                _appState.update { it.copy(isConnected = isConnected) }

                if (isConnected && !previousState) {
                    soundManager.playConnectedSound()
                    _notification.emit("Conectado a Internet")
                    // Auto-sync when connection is restored
                    syncData()
                } else if (!isConnected && previousState) {
                    soundManager.playDisconnectedSound()
                    _notification.emit("Sin conexión a Internet - Modo offline")
                }
            }
        }
    }

    // Category operations
    fun createCategory(name: String, description: String) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val category = Category(
                id = "",
                name = name,
                description = description
            )
            val result = repository.insertCategory(category)
            if (result.isSuccess) {
                soundManager.playCreateSound()
                _notification.emit("Categoría creada: $name")
                _appState.update { it.copy(uiState = UiState.Success("Categoría creada")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al crear categoría"))
                }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val result = repository.updateCategory(category)
            if (result.isSuccess) {
                soundManager.playUpdateSound()
                _notification.emit("Categoría actualizada: ${category.name}")
                _appState.update { it.copy(uiState = UiState.Success("Categoría actualizada")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al actualizar categoría"))
                }
            }
        }
    }

    fun deleteCategory(id: String, name: String) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val result = repository.deleteCategory(id)
            if (result.isSuccess) {
                soundManager.playDeleteSound()
                _notification.emit("Categoría eliminada: $name")
                _appState.update { it.copy(uiState = UiState.Success("Categoría eliminada")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al eliminar categoría"))
                }
            }
        }
    }

    // Product operations
    fun createProduct(
        name: String,
        description: String,
        price: Double,
        categoryId: String,
        stock: Int
    ) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val product = Product(
                id = "",
                name = name,
                description = description,
                price = price,
                categoryId = categoryId,
                stock = stock
            )
            val result = repository.insertProduct(product)
            if (result.isSuccess) {
                soundManager.playCreateSound()
                _notification.emit("Producto creado: $name")
                _appState.update { it.copy(uiState = UiState.Success("Producto creado")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al crear producto"))
                }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val result = repository.updateProduct(product)
            if (result.isSuccess) {
                soundManager.playUpdateSound()
                _notification.emit("Producto actualizado: ${product.name}")
                _appState.update { it.copy(uiState = UiState.Success("Producto actualizado")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al actualizar producto"))
                }
            }
        }
    }

    fun deleteProduct(id: String, name: String) {
        viewModelScope.launch {
            _appState.update { it.copy(uiState = UiState.Loading) }
            val result = repository.deleteProduct(id)
            if (result.isSuccess) {
                soundManager.playDeleteSound()
                _notification.emit("Producto eliminado: $name")
                _appState.update { it.copy(uiState = UiState.Success("Producto eliminado")) }
            } else {
                _appState.update { 
                    it.copy(uiState = UiState.Error("Error al eliminar producto"))
                }
            }
        }
    }

    fun selectCategory(category: Category?) {
        _appState.update { it.copy(selectedCategory = category) }
        if (category != null) {
            viewModelScope.launch {
                soundManager.playReadSound()
            }
        }
    }

    // Sync operation
    fun syncData() {
        viewModelScope.launch {
            _appState.update { it.copy(isSyncing = true) }
            val result = repository.syncData()
            if (result.isSuccess) {
                soundManager.playSyncSound()
                _notification.emit("Sincronización completada")
            } else {
                _notification.emit("Error en sincronización: ${result.exceptionOrNull()?.message}")
            }
            _appState.update { it.copy(isSyncing = false) }
        }
    }

    fun clearUiState() {
        _appState.update { it.copy(uiState = UiState.Idle) }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
