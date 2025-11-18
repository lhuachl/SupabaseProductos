package com.example.supabaseproductos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabaseproductos.data.local.entities.Category
import com.example.supabaseproductos.data.local.entities.Product
import com.example.supabaseproductos.ui.components.*
import com.example.supabaseproductos.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appState: AppState,
    notification: String?,
    onCreateCategory: (String, String) -> Unit,
    onUpdateCategory: (Category) -> Unit,
    onDeleteCategory: (String, String) -> Unit,
    onCreateProduct: (String, String, Double, String, Int) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (String, String) -> Unit,
    onSelectCategory: (Category?) -> Unit,
    onSync: () -> Unit,
    onDismissNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showDeleteCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteProductDialog by remember { mutableStateOf<Product?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Supabase Productos") },
                    actions = {
                        SyncButton(
                            isSyncing = appState.isSyncing,
                            isConnected = appState.isConnected,
                            onClick = onSync
                        )
                    }
                )
                ConnectionStatusBanner(isConnected = appState.isConnected)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        editingCategory = null
                        showCategoryDialog = true
                    } else {
                        editingProduct = null
                        showProductDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Categorías") },
                        icon = { Icon(Icons.Default.Category, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Productos") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                    )
                }

                when (selectedTab) {
                    0 -> CategoriesTab(
                        categories = appState.categories,
                        onEdit = { category ->
                            editingCategory = category
                            showCategoryDialog = true
                        },
                        onDelete = { category ->
                            showDeleteCategoryDialog = category
                        },
                        onSelect = onSelectCategory
                    )
                    1 -> ProductsTab(
                        products = appState.products,
                        selectedCategory = appState.selectedCategory,
                        onEdit = { product ->
                            editingProduct = product
                            showProductDialog = true
                        },
                        onDelete = { product ->
                            showDeleteProductDialog = product
                        }
                    )
                }
            }

            // Notification overlay
            notification?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    AnimatedNotification(
                        message = it,
                        onDismiss = onDismissNotification
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCategoryDialog) {
        CategoryDialog(
            category = editingCategory,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, description ->
                if (editingCategory != null) {
                    onUpdateCategory(editingCategory!!.copy(name = name, description = description))
                } else {
                    onCreateCategory(name, description)
                }
                showCategoryDialog = false
            }
        )
    }

    if (showProductDialog) {
        ProductDialog(
            product = editingProduct,
            categories = appState.categories,
            onDismiss = { showProductDialog = false },
            onSave = { name, description, price, categoryId, stock ->
                if (editingProduct != null) {
                    onUpdateProduct(
                        editingProduct!!.copy(
                            name = name,
                            description = description,
                            price = price,
                            categoryId = categoryId,
                            stock = stock
                        )
                    )
                } else {
                    onCreateProduct(name, description, price, categoryId, stock)
                }
                showProductDialog = false
            }
        )
    }

    showDeleteCategoryDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = null },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Estás seguro de que deseas eliminar la categoría '${category.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(category.id, category.name)
                        showDeleteCategoryDialog = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCategoryDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showDeleteProductDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteProductDialog = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que deseas eliminar el producto '${product.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(product.id, product.name)
                        showDeleteProductDialog = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProductDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CategoriesTab(
    categories: List<Category>,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay categorías",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(categories, key = { it.id }) { category ->
                CategoryItem(
                    category = category,
                    onClick = { onSelect(category) },
                    onEdit = { onEdit(category) },
                    onDelete = { onDelete(category) }
                )
            }
        }
    }
}

@Composable
fun ProductsTab(
    products: List<Product>,
    selectedCategory: Category?,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredProducts = if (selectedCategory != null) {
        products.filter { it.categoryId == selectedCategory.id }
    } else {
        products
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (selectedCategory != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categoría: ${selectedCategory.name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCategory != null) 
                            "No hay productos en esta categoría" 
                        else 
                            "No hay productos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductItem(
                        product = product,
                        onClick = { },
                        onEdit = { onEdit(product) },
                        onDelete = { onDelete(product) }
                    )
                }
            }
        }
    }
}
