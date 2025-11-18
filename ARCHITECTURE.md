# Arquitectura y Decisiones de Diseño

## Visión General

Esta aplicación implementa un patrón de arquitectura offline-first con sincronización en la nube, siguiendo las mejores prácticas de Android moderno.

## Arquitectura en Capas

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Screens  │  │Components│  │  Theme & Styles  │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              ViewModel Layer                        │
│  ┌──────────────────────────────────────────────┐  │
│  │  MainViewModel (State Management)            │  │
│  │  - AppState (StateFlow)                      │  │
│  │  - Notification (SharedFlow)                 │  │
│  │  - Business Logic                            │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              Repository Layer                       │
│  ┌──────────────────────────────────────────────┐  │
│  │  Repository (Unified Data Source)            │  │
│  │  - Decides local vs remote                   │  │
│  │  - Manages sync state                        │  │
│  │  - Conflict resolution                       │  │
│  └──────────────────────────────────────────────┘  │
└────────┬───────────────────────────────┬───────────┘
         │                               │
┌────────▼──────────┐         ┌─────────▼────────────┐
│  Local Data Layer │         │ Remote Data Layer    │
│  ┌──────────────┐ │         │ ┌─────────────────┐ │
│  │ Room Database│ │         │ │Supabase Client  │ │
│  │  - DAOs      │ │         │ │  - Postgrest    │ │
│  │  - Entities  │ │         │ │  - Realtime     │ │
│  └──────────────┘ │         │ └─────────────────┘ │
└───────────────────┘         └─────────────────────┘
```

## Componentes Principales

### 1. Data Layer (Capa de Datos)

#### 1.1 Entidades (Entities)
**Ubicación:** `data/local/entities/`

```kotlin
// Campos comunes en todas las entidades
- id: String (UUID)
- createdAt: Long (timestamp)
- updatedAt: Long (timestamp)
- isSynced: Boolean (estado de sincronización)
- isDeleted: Boolean (soft delete)
```

**Decisiones:**
- **UUID como String**: Permite generar IDs en el cliente sin conflictos
- **Timestamps Long**: Milisegundos desde epoch, independiente de zona horaria
- **Soft Delete**: Permite sincronizar eliminaciones y recuperar datos
- **isSynced flag**: Rastrea qué necesita sincronizarse

#### 1.2 Room Database
**Ubicación:** `data/local/AppDatabase.kt`

**Decisiones:**
- **Singleton Pattern**: Una única instancia de la base de datos
- **Flow para consultas**: Actualizaciones reactivas automáticas
- **FallbackToDestructiveMigration**: Simplifica desarrollo (cambiar en producción)

#### 1.3 DAOs
**Ubicación:** `data/local/dao/`

**Operaciones Clave:**
```kotlin
// CRUD básico
- insert(entity): Crea nuevo registro
- update(entity): Actualiza existente
- softDelete(id): Marca como eliminado
- getAll(): Flow<List> (reactivo)

// Sincronización
- getUnsynced(): Obtiene pendientes de sync
- markAsSynced(id): Marca como sincronizado
- cleanupSyncedDeleted(): Limpia eliminados sincronizados
```

#### 1.4 Supabase Client
**Ubicación:** `data/remote/`

**Decisiones:**
- **Lazy initialization**: Cliente se crea cuando se necesita
- **Instalación modular**: Postgrest, Realtime, Storage por separado
- **BuildConfig para credenciales**: Seguridad y flexibilidad

### 2. Repository Layer (Capa de Repositorio)

**Ubicación:** `data/repository/Repository.kt`

**Responsabilidades:**
1. **Abstracción**: UI no conoce origen de datos
2. **Decisión de fuente**: Local vs remota según conectividad
3. **Sincronización**: Coordina local y remota
4. **Gestión de errores**: Manejo robusto de fallos

**Flujo de Operaciones CRUD:**

```
CREATE/UPDATE/DELETE
├─ ¿Conectado?
│  ├─ SÍ: Intenta remota primero
│  │  ├─ Éxito: Guarda local con isSynced=true
│  │  └─ Fallo: Guarda local con isSynced=false
│  └─ NO: Guarda local con isSynced=false
└─ Retorna Result<T>
```

**Sincronización Bidireccional:**

```
SYNC
├─ Sube datos locales no sincronizados
│  ├─ Categorías nuevas/actualizadas
│  ├─ Categorías eliminadas
│  ├─ Productos nuevos/actualizados
│  └─ Productos eliminados
├─ Descarga datos remotos
│  ├─ Categorías desde Supabase
│  └─ Productos desde Supabase
└─ Limpia eliminados sincronizados
```

### 3. ViewModel Layer

**Ubicación:** `viewmodel/MainViewModel.kt`

**Patrón de Estado:**
```kotlin
data class AppState(
    val categories: List<Category>,
    val products: List<Product>,
    val isConnected: Boolean,
    val isSyncing: Boolean,
    val selectedCategory: Category?,
    val uiState: UiState
)
```

**Decisiones:**
- **StateFlow**: Estado inmutable y observable
- **SharedFlow**: Eventos únicos (notificaciones)
- **Coroutines**: Operaciones asíncronas
- **Error handling**: Manejo centralizado

### 4. UI Layer (Jetpack Compose)

#### 4.1 Screens
**Ubicación:** `ui/screens/`

- **MainScreen**: Pantalla principal con tabs
- **Scaffold**: Estructura Material 3
- **Tabs**: Categorías y Productos

#### 4.2 Components
**Ubicación:** `ui/components/`

**Componentes Reutilizables:**
- **AnimatedNotification**: Notificaciones con animaciones
- **ConnectionStatusBanner**: Indicador de conectividad
- **SyncButton**: Botón de sincronización animado
- **CategoryItem/ProductItem**: Cards de lista
- **CategoryDialog/ProductDialog**: Formularios

**Decisiones de Animación:**
```kotlin
// Spring animation para entrada natural
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

// Tween para salidas suaves
tween(durationMillis = 300)
```

### 5. Utilities

#### 5.1 NetworkMonitor
**Ubicación:** `util/NetworkMonitor.kt`

**Funcionalidad:**
- Observa cambios de conectividad en tiempo real
- Flow reactivo para UI
- Compatible con diferentes tipos de red (WiFi, móvil)

**Implementación:**
```kotlin
// Usa NetworkCallback de Android
- onAvailable(): Red disponible
- onLost(): Red perdida
- onCapabilitiesChanged(): Cambio en capacidades
```

#### 5.2 SoundManager
**Ubicación:** `util/SoundManager.kt`

**Tonos Implementados:**
```kotlin
- CREATE: TONE_PROP_BEEP (200ms, ascendente)
- UPDATE: TONE_PROP_ACK (150ms, doble beep)
- DELETE: TONE_PROP_NACK (250ms, descendente)
- READ: TONE_PROP_BEEP2 (100ms, corto)
- SYNC: TONE_CDMA_ALERT_CALL_GUARD (300ms, éxito)
- CONNECTED: TONE_CDMA_KEYPAD_VOLUME_KEY_LITE (200ms)
- DISCONNECTED: TONE_CDMA_ABBR_ALERT (200ms)
```

**Decisiones:**
- **ToneGenerator**: API nativa, sin archivos
- **STREAM_NOTIFICATION**: Canal apropiado
- **Volume 100**: Balance entre audible y no intrusivo
- **Coroutines Dispatchers.IO**: No bloquea UI

### 6. Background Services

#### 6.1 SyncWorker
**Ubicación:** `service/SyncWorker.kt`

**Configuración:**
```kotlin
- Periodicidad: 15 minutos
- Constraint: NETWORK_CONNECTED
- Backoff: LINEAR
- Política: KEEP (no duplica)
```

**Flujo:**
```
WorkManager
├─ Verifica conectividad
├─ Ejecuta sincronización
├─ Reproduce sonido de éxito
└─ Retorna resultado (Success/Retry/Failure)
```

## Patrones de Diseño Aplicados

### 1. Repository Pattern
- Abstrae la fuente de datos
- Facilita testing
- Centraliza lógica de datos

### 2. MVVM (Model-View-ViewModel)
- Separación de responsabilidades
- UI reactiva
- Testeable

### 3. Observer Pattern
- Flow/StateFlow para reactividad
- UI actualizada automáticamente
- Desacoplamiento

### 4. Singleton Pattern
- Database, Supabase client
- Única instancia compartida

### 5. Strategy Pattern
- Decisión local vs remoto
- Basado en conectividad

## Decisiones Técnicas Clave

### 1. Offline-First vs Online-First

**Elegido: Offline-First**

**Razones:**
- Mejor experiencia de usuario (sin esperas)
- Funciona sin conexión
- Sincronización en segundo plano
- Mayor resiliencia

**Trade-offs:**
- Mayor complejidad de código
- Gestión de conflictos necesaria
- Espacio de almacenamiento local

### 2. Soft Delete vs Hard Delete

**Elegido: Soft Delete**

**Razones:**
- Permite sincronizar eliminaciones
- Posibilidad de recuperar datos
- Auditoría de cambios

**Implementación:**
```kotlin
isDeleted = true // Marca como eliminado
isSynced = false // Pendiente de sincronizar
// Después de sync exitoso:
cleanup() // Elimina físicamente
```

### 3. UUID Client-Side vs Server-Side

**Elegido: Client-Side UUID**

**Razones:**
- Sin dependencia de servidor para IDs
- Creación offline
- Sin colisiones (probabilidad infinitesimal)
- Sincronización simplificada

### 4. Timestamps vs Server Timestamps

**Elegido: Client Timestamps**

**Razones:**
- Funciona offline
- Suficiente para resolución de conflictos
- Menor latencia

**Consideraciones:**
- Requiere clocks sincronizados
- Conflictos resueltos por "último gana"

### 5. StateFlow vs LiveData

**Elegido: StateFlow**

**Razones:**
- Más moderno y idiomático en Kotlin
- Mejor integración con Coroutines
- Lifecycle-aware en Compose
- Operators más poderosos

### 6. Jetpack Compose vs XML

**Elegido: Jetpack Compose**

**Razones:**
- Declarativo y moderno
- Menos boilerplate
- Animaciones más fáciles
- Mejor desarrollo de UI
- Futuro de Android UI

### 7. Room vs SQLite Directo

**Elegido: Room**

**Razones:**
- Type-safe
- Compile-time verification
- Fácil integración con Flow
- LiveData/Flow out-of-the-box
- Menos errores

### 8. WorkManager vs Service

**Elegido: WorkManager**

**Razones:**
- Respeta battery optimization
- Garantiza ejecución eventual
- Constraints (red, batería, etc.)
- Backward compatibility
- Fácil de usar

## Gestión de Conflictos

### Estrategia: Last Write Wins (LWW)

**Implementación:**
```kotlin
if (local.updatedAt > remote.updatedAt) {
    // Local más reciente, subir a remoto
    uploadToRemote(local)
} else {
    // Remoto más reciente, actualizar local
    updateLocal(remote)
}
```

**Limitaciones:**
- Puede perder cambios concurrentes
- No detecta conflictos semánticos

**Alternativas Futuras:**
- CRDT (Conflict-free Replicated Data Types)
- Vector clocks
- Merge con UI de resolución

## Consideraciones de Seguridad

### Actual (Desarrollo)
- RLS con políticas públicas
- Supabase anon key en código
- Sin autenticación de usuario

### Producción (Recomendado)
```kotlin
// 1. Implementar autenticación
install(GoTrue)

// 2. User ID en todas las entidades
data class Category(
    val userId: UUID,
    // ... otros campos
)

// 3. RLS basado en usuario
CREATE POLICY "Users see own data" ON categories
FOR ALL USING (auth.uid() = user_id);

// 4. Credenciales en env variables
val supabaseUrl = System.getenv("SUPABASE_URL")
val supabaseKey = System.getenv("SUPABASE_KEY")
```

## Performance

### Optimizaciones Implementadas

1. **Lazy Loading**
   - LazyColumn para listas
   - Solo renderiza elementos visibles

2. **Database Indexing**
   - Índices en campos frecuentemente consultados
   - `isDeleted`, `categoryId`

3. **Coroutines**
   - Operaciones no bloquean UI
   - Dispatchers apropiados (IO, Main)

4. **Caching**
   - Room como cache local
   - Reduce llamadas a red

5. **Minimal Recomposition**
   - remember para estados locales
   - derivedStateOf para cálculos

### Métricas a Monitorear

- Tiempo de sincronización
- Tamaño de base de datos local
- Memoria utilizada
- Batería consumida por sync
- Latencia de operaciones

## Testing Strategy

### Unit Tests
```kotlin
// ViewModels
- Estado inicial correcto
- Operaciones CRUD actualizan estado
- Manejo de errores

// Repository
- Lógica de offline/online
- Sincronización correcta
- Resolución de conflictos
```

### Integration Tests
```kotlin
// Database
- Operaciones CRUD
- Queries complejas
- Constraints e integridad

// Sync
- Flujo completo de sincronización
- Escenarios de error
```

### UI Tests
```kotlin
// Screens
- Navegación
- Input validation
- Interacciones de usuario
```

## Mejoras Futuras

### Corto Plazo
1. Implementar autenticación
2. Añadir búsqueda y filtros
3. Paginación para listas grandes
4. Mejores animaciones de transición

### Mediano Plazo
1. Imágenes de productos (con Storage)
2. Escaneo de códigos de barras
3. Exportar/importar datos (CSV, JSON)
4. Modo dark/light personalizable
5. Analytics y métricas

### Largo Plazo
1. Multi-usuario con colaboración
2. Historial de cambios y auditoría
3. Resolución de conflictos inteligente
4. Integración con sistemas externos
5. Dashboard web complementario

## Referencias

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/performance)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Supabase Documentation](https://supabase.com/docs)
- [Material Design 3](https://m3.material.io/)

## Conclusión

Esta arquitectura proporciona:
- ✅ Experiencia offline completa
- ✅ Sincronización automática confiable
- ✅ UI moderna y responsiva
- ✅ Código mantenible y testeable
- ✅ Escalabilidad para futuras features
- ✅ Seguridad con potencial para producción

La aplicación está lista para desarrollo adicional y puede servir como base para aplicaciones offline-first más complejas.
