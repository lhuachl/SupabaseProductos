package com.example.supabaseproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supabaseproductos.ui.screens.MainScreen
import com.example.supabaseproductos.ui.theme.SupabaseProductosTheme
import com.example.supabaseproductos.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupabaseProductosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val appState by viewModel.appState.collectAsStateWithLifecycle()
                    val notification by viewModel.notification.collectAsState(initial = null)

                    MainScreen(
                        appState = appState,
                        notification = notification,
                        onCreateCategory = { name, description ->
                            viewModel.createCategory(name, description)
                        },
                        onUpdateCategory = { category ->
                            viewModel.updateCategory(category)
                        },
                        onDeleteCategory = { id, name ->
                            viewModel.deleteCategory(id, name)
                        },
                        onCreateProduct = { name, description, price, categoryId, stock ->
                            viewModel.createProduct(name, description, price, categoryId, stock)
                        },
                        onUpdateProduct = { product ->
                            viewModel.updateProduct(product)
                        },
                        onDeleteProduct = { id, name ->
                            viewModel.deleteProduct(id, name)
                        },
                        onSelectCategory = { category ->
                            viewModel.selectCategory(category)
                        },
                        onSync = {
                            viewModel.syncData()
                        },
                        onDismissNotification = {
                            // Notification is auto-dismissed in the component
                        }
                    )
                }
            }
        }
    }
}
