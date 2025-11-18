# Supabase Productos - Offline-First Android App

Una aplicación Android moderna que implementa un sistema de gestión de productos y categorías con sincronización en la nube, funcionalidad offline completa, notificaciones en tiempo real y retroalimentación de sonido generada por dispositivo.

## Características Principales

### 🗄️ Base de Datos Dual
- **SQLite Local (Room)**: Almacenamiento offline persistente
- **Supabase Cloud**: Sincronización en la nube con PostgreSQL
- **Sincronización Automática**: Sincroniza automáticamente cuando se detecta conexión a Internet

### 🔄 Gestión de Conectividad
- **Detección de Red**: Monitoreo en tiempo real del estado de conexión
- **Modo Offline**: Todas las operaciones CRUD funcionan sin Internet
- **Cola de Sincronización**: Las operaciones offline se sincronizan automáticamente al reconectar

### 📱 Operaciones CRUD Completas
- **Categorías**: Crear, leer, actualizar, eliminar
- **Productos**: Crear, leer, actualizar, eliminar con campos precio, stock, y categoría
- **Soft Delete**: Eliminación suave con sincronización posterior
- **Estado de Sincronización**: Indicador visual para elementos no sincronizados

### 🔔 Notificaciones en Tiempo Real
- **Notificaciones Animadas**: Feedback visual con animaciones suaves
- **Eventos CRUD**: Notificación para cada operación
- **Estado de Conectividad**: Alertas de conexión/desconexión
- **Animaciones**: Entrada/salida animada con spring animations

### 🔊 Sonidos Generados por Dispositivo
- **ToneGenerator**: Usa la API nativa de Android para generar tonos
- **Sin Archivos de Audio**: No requiere descargar o incluir archivos de sonido
- **Tonos Específicos**:
  - Creación: Tono ascendente agudo
  - Actualización: Doble beep medio
  - Eliminación: Tono descendente grave
  - Lectura: Beep corto
  - Sincronización: Tono de éxito
  - Conectado: Tono de conexión establecida
  - Desconectado: Tono de alerta

### 🎨 UI Moderna con Jetpack Compose
- **Material Design 3**: Interfaz moderna y atractiva
- **Animaciones Fluidas**: Transiciones y feedback visual
- **Tema Responsivo**: Soporte para modo claro/oscuro
- **Componentes Reutilizables**: Arquitectura modular

## Arquitectura

### Capas de la Aplicación

```
app/
├── data/
│   ├── local/              # Room Database
│   │   ├── entities/       # Category & Product entities
│   │   ├── dao/            # Data Access Objects
│   │   └── AppDatabase.kt  # Room Database
│   ├── remote/             # Supabase Integration
│   │   ├── SupabaseClientProvider.kt
│   │   └── SupabaseDataSource.kt
│   └── repository/         # Repository Pattern
│       └── Repository.kt   # Unified data source
├── viewmodel/              # ViewModel Layer
│   └── MainViewModel.kt    # App state management
├── ui/
│   ├── screens/            # Composable Screens
│   ├── components/         # Reusable UI Components
│   └── theme/              # Material Theme
├── util/                   # Utilities
│   ├── NetworkMonitor.kt   # Connectivity detection
│   └── SoundManager.kt     # Sound generation
└── service/                # Background Services
    └── SyncWorker.kt       # WorkManager sync worker
```

### Tecnologías Utilizadas

- **Kotlin 2.0.21** - Lenguaje de programación moderno
- **Jetpack Compose** - Framework de UI declarativa con Compose Compiler Plugin
- **Room** - Base de datos SQLite local
- **Supabase** - Backend as a Service (PostgreSQL + Realtime)
- **Coroutines & Flow** - Programación asíncrona y reactiva
- **WorkManager** - Sincronización en segundo plano
- **Material Design 3** - Sistema de diseño
- **ViewModel & StateFlow** - Gestión de estado
- **ToneGenerator** - Generación de sonidos del sistema

### Configuración de Jetpack Compose

Este proyecto utiliza la configuración moderna de Jetpack Compose con Kotlin 2.0+:

- **Compose Compiler Plugin** (`org.jetbrains.kotlin.plugin.compose`) - Gestión automática de versiones del compilador
- **Compose BOM** - Gestión centralizada de versiones de bibliotecas Compose
- Sin necesidad de configurar manualmente `kotlinCompilerExtensionVersion`

Para más detalles sobre la configuración de Compose, consulta [COMPOSE_CONFIGURATION.md](COMPOSE_CONFIGURATION.md).

## Configuración

### Requisitos Previos

- Android Studio Hedgehog o superior
- JDK 11 o superior
- Cuenta de Supabase (gratuita)

### Configuración de Supabase

1. Crea un proyecto en [Supabase](https://supabase.com)
2. Crea las siguientes tablas en SQL Editor:

```sql
-- Tabla de categorías
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_synced BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false
);

-- Tabla de productos
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    category_id UUID REFERENCES categories(id),
    stock INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_synced BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false
);

-- Habilitar Row Level Security (RLS)
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Políticas de acceso público (para desarrollo)
CREATE POLICY "Enable all access for categories" ON categories FOR ALL USING (true);
CREATE POLICY "Enable all access for products" ON products FOR ALL USING (true);

-- Habilitar Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE categories;
ALTER PUBLICATION supabase_realtime ADD TABLE products;
```

3. Obtén tu URL y Anon Key desde Project Settings > API
4. Actualiza `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://tu-proyecto.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"tu-anon-key\"")
```

### Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/lhuachl/SupabaseProductos.git
cd SupabaseProductos
```

2. Abre el proyecto en Android Studio

3. Configura las credenciales de Supabase en `app/build.gradle.kts`

4. Sincroniza el proyecto con Gradle

5. Ejecuta la aplicación en un emulador o dispositivo físico

## Uso

### Gestión de Categorías

1. **Crear**: Toca el botón FAB (+) en la pestaña "Categorías"
2. **Editar**: Toca el menú (⋮) en una categoría y selecciona "Editar"
3. **Eliminar**: Toca el menú (⋮) y selecciona "Eliminar"
4. **Ver Productos**: Toca una categoría para filtrar productos

### Gestión de Productos

1. **Crear**: Toca el botón FAB (+) en la pestaña "Productos"
2. **Editar**: Toca el menú (⋮) en un producto y selecciona "Editar"
3. **Eliminar**: Toca el menú (⋮) y selecciona "Eliminar"

### Sincronización

- **Automática**: Se sincroniza al detectar conexión a Internet
- **Manual**: Toca el botón "Sincronizar" en la barra superior
- **Background**: WorkManager sincroniza cada 15 minutos

### Indicadores

- **Estado de Conexión**: Banner superior (verde = online, rojo = offline)
- **Elementos No Sincronizados**: Icono de nube con mensaje en cada item
- **Sincronización en Progreso**: Botón de sincronizar con animación

## Características Técnicas

### Gestión de Estado
- StateFlow para estado reactivo
- SharedFlow para eventos únicos (notificaciones)
- Lifecycle-aware observers

### Sincronización
- Detección de conflictos
- Timestamps para resolución de conflictos
- Soft delete con limpieza automática
- Reintentos automáticos con backoff

### Performance
- Lazy loading con LazyColumn
- Coroutines para operaciones asíncronas
- Room database caching
- Minimal recompositions

### Seguridad
- Row Level Security en Supabase
- Validación de inputs
- Error handling robusto

## Testing

La aplicación incluye estructura para:
- Unit tests para ViewModel
- Integration tests para Repository
- UI tests con Compose Testing

## Roadmap

- [ ] Búsqueda y filtrado avanzado
- [ ] Exportar/Importar datos
- [ ] Imágenes de productos
- [ ] Código de barras/QR
- [ ] Múltiples usuarios
- [ ] Analytics y reportes

## Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## Contacto

GitHub: [@lhuachl](https://github.com/lhuachl)

## Agradecimientos

- Supabase por su excelente BaaS
- Jetpack Compose por la UI moderna
- Comunidad Android por los recursos
