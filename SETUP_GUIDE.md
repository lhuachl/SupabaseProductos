# Guía de Configuración - Supabase Productos

Esta guía te ayudará a configurar el proyecto desde cero.

## Paso 1: Configurar Supabase

### 1.1 Crear Proyecto en Supabase

1. Visita [supabase.com](https://supabase.com)
2. Crea una cuenta gratuita si no tienes una
3. Haz clic en "New Project"
4. Completa los datos:
   - **Name**: SupabaseProductos (o el nombre que prefieras)
   - **Database Password**: Guarda esta contraseña de forma segura
   - **Region**: Selecciona la región más cercana a ti
5. Haz clic en "Create new project"
6. Espera a que el proyecto termine de inicializarse

### 1.2 Crear la Base de Datos

1. En el panel de Supabase, ve a **SQL Editor** en el menú lateral
2. Haz clic en "New Query"
3. Copia y pega el siguiente script SQL:

```sql
-- Habilitar la extensión UUID (si no está habilitada)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de categorías
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_synced BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false
);

-- Tabla de productos
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    stock INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_synced BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_categories_deleted ON categories(is_deleted);
CREATE INDEX IF NOT EXISTS idx_products_deleted ON products(is_deleted);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);

-- Habilitar Row Level Security (RLS)
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Políticas de acceso público (para desarrollo)
-- IMPORTANTE: En producción, debes configurar políticas más restrictivas
DROP POLICY IF EXISTS "Enable all access for categories" ON categories;
CREATE POLICY "Enable all access for categories" 
ON categories FOR ALL 
USING (true) 
WITH CHECK (true);

DROP POLICY IF EXISTS "Enable all access for products" ON products;
CREATE POLICY "Enable all access for products" 
ON products FOR ALL 
USING (true) 
WITH CHECK (true);

-- Habilitar Realtime para actualizaciones en tiempo real
ALTER PUBLICATION supabase_realtime ADD TABLE categories;
ALTER PUBLICATION supabase_realtime ADD TABLE products;

-- Insertar datos de ejemplo (opcional)
INSERT INTO categories (id, name, description, created_at, updated_at, is_synced, is_deleted) 
VALUES 
    (uuid_generate_v4(), 'Electrónica', 'Productos electrónicos y gadgets', extract(epoch from now()) * 1000, extract(epoch from now()) * 1000, true, false),
    (uuid_generate_v4(), 'Alimentos', 'Productos alimenticios', extract(epoch from now()) * 1000, extract(epoch from now()) * 1000, true, false),
    (uuid_generate_v4(), 'Ropa', 'Indumentaria y accesorios', extract(epoch from now()) * 1000, extract(epoch from now()) * 1000, true, false)
ON CONFLICT DO NOTHING;
```

4. Haz clic en "Run" o presiona `Ctrl+Enter`
5. Verifica que se ejecutó correctamente (deberías ver "Success. No rows returned")

### 1.3 Obtener las Credenciales de API

1. Ve a **Project Settings** (engranaje en el menú lateral)
2. Selecciona **API** en el submenú
3. Copia los siguientes valores:
   - **Project URL**: Algo como `https://xyzcompany.supabase.co`
   - **anon public**: La clave API pública (anon key)

## Paso 2: Configurar Android Studio

### 2.1 Actualizar las Credenciales

1. Abre el proyecto en Android Studio
2. Navega a `app/build.gradle.kts`
3. Encuentra la sección `buildConfigField` (líneas 22-23)
4. Reemplaza los valores con tus credenciales:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://TU-PROYECTO.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"tu-anon-key-aqui\"")
```

**Ejemplo:**
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://abcdefgh.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
```

### 2.2 Sincronizar el Proyecto

1. Haz clic en "Sync Now" cuando Android Studio lo solicite
2. O selecciona **File > Sync Project with Gradle Files**
3. Espera a que se descarguen todas las dependencias

## Paso 3: Ejecutar la Aplicación

### 3.1 Configurar el Emulador o Dispositivo

**Opción A: Emulador**
1. Ve a **Device Manager** en Android Studio
2. Crea un nuevo dispositivo virtual (AVD)
3. Selecciona un dispositivo (recomendado: Pixel 6)
4. Selecciona una imagen del sistema (API 34 o superior)
5. Haz clic en "Finish"

**Opción B: Dispositivo Físico**
1. Habilita "Opciones de desarrollador" en tu dispositivo Android
2. Habilita "Depuración USB"
3. Conecta el dispositivo a tu computadora
4. Acepta el mensaje de depuración USB en el dispositivo

### 3.2 Compilar y Ejecutar

1. Selecciona tu dispositivo/emulador en la barra de herramientas
2. Haz clic en el botón "Run" (▶️) o presiona `Shift+F10`
3. Espera a que la aplicación se compile e instale
4. La aplicación debería abrirse automáticamente

## Paso 4: Probar la Aplicación

### 4.1 Probar Modo Online

1. Asegúrate de que tu dispositivo/emulador tenga conexión a Internet
2. El banner superior debería mostrar "En línea" en verde
3. Crea una categoría:
   - Toca el botón flotante (+)
   - Ingresa nombre y descripción
   - Toca "Guardar"
   - Deberías escuchar un sonido y ver una notificación
4. Verifica en Supabase:
   - Ve a **Table Editor** en Supabase
   - Selecciona la tabla `categories`
   - Deberías ver tu categoría recién creada

### 4.2 Probar Modo Offline

1. **En Emulador:**
   - Arrastra desde la parte superior de la pantalla
   - Desactiva WiFi y datos móviles
   
2. **En Dispositivo:**
   - Activa el modo avión

3. El banner debería cambiar a "Sin conexión" en rojo y escucharás un sonido
4. Crea otra categoría mientras estás offline
5. Nota el icono de nube con mensaje "Pendiente de sincronización"
6. Los datos se guardan localmente en SQLite

### 4.3 Probar Sincronización

1. Reactiva la conexión a Internet
2. El banner cambiará a verde y escucharás el sonido de conexión
3. La app sincronizará automáticamente los datos pendientes
4. Verás una notificación "Sincronización completada"
5. El icono de nube desaparecerá de los elementos
6. Verifica en Supabase que los datos offline ahora están en la nube

### 4.4 Probar Productos

1. Primero crea al menos una categoría
2. Ve a la pestaña "Productos"
3. Toca el botón (+)
4. Completa el formulario:
   - Nombre
   - Descripción
   - Precio (usa punto para decimales, ej: 29.99)
   - Stock (número entero)
   - Categoría (selecciona del dropdown)
5. Toca "Guardar"
6. Verifica que el producto aparece en la lista
7. Prueba editar y eliminar productos

### 4.5 Probar Notificaciones y Sonidos

Cada operación debería producir:
- **Crear**: Notificación animada + tono ascendente
- **Actualizar**: Notificación + doble beep
- **Eliminar**: Notificación + tono descendente
- **Sincronizar**: Notificación "Sincronización completada" + tono de éxito
- **Conectar**: Notificación "Conectado" + tono de conexión
- **Desconectar**: Notificación "Sin conexión" + tono de alerta

## Troubleshooting (Solución de Problemas)

### Error: "Unable to resolve dependency"

**Solución:**
1. Verifica tu conexión a Internet
2. En Android Studio, ve a **File > Invalidate Caches / Restart**
3. Selecciona "Invalidate and Restart"
4. Espera a que se reindexe el proyecto

### Error: "BuildConfig does not exist"

**Solución:**
1. Asegúrate de haber sincronizado el proyecto con Gradle
2. Verifica que `buildFeatures { buildConfig = true }` está en `build.gradle.kts`
3. Limpia y reconstruye: **Build > Clean Project**, luego **Build > Rebuild Project**

### La aplicación no sincroniza

**Solución:**
1. Verifica que las credenciales de Supabase son correctas
2. Verifica que las tablas existen en Supabase
3. Revisa Logcat en Android Studio para ver errores específicos
4. Asegúrate de que las políticas RLS están configuradas correctamente

### No se escuchan los sonidos

**Solución:**
1. Verifica que el volumen de notificaciones del dispositivo no está en 0
2. En dispositivos físicos, verifica que no está en modo silencioso
3. Los tonos se generan con ToneGenerator, no requieren archivos de audio

### Error de red "Failed to connect"

**Solución:**
1. Verifica la URL de Supabase (debe empezar con https://)
2. Verifica que el proyecto de Supabase está activo
3. Intenta acceder a la URL en un navegador para confirmar que funciona
4. Verifica los permisos de Internet en AndroidManifest.xml

## Arquitectura de Seguridad

### Para Producción

Las políticas RLS actuales permiten acceso público completo. Para producción:

1. **Implementa Autenticación:**
```sql
-- Política basada en usuario autenticado
CREATE POLICY "Users can only see their own data" ON categories
FOR SELECT USING (auth.uid() = user_id);
```

2. **Agrega campo de usuario a las tablas:**
```sql
ALTER TABLE categories ADD COLUMN user_id UUID REFERENCES auth.users(id);
ALTER TABLE products ADD COLUMN user_id UUID REFERENCES auth.users(id);
```

3. **Implementa Supabase Auth en la app:**
```kotlin
// Agregar dependencia
implementation("io.github.jan-tennert.supabase:gotrue-kt")

// Configurar en SupabaseClientProvider
install(GoTrue)
```

## Recursos Adicionales

- [Documentación de Supabase](https://supabase.com/docs)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

## Soporte

Si encuentras problemas:
1. Revisa la sección de Troubleshooting anterior
2. Busca en [Issues de GitHub](https://github.com/lhuachl/SupabaseProductos/issues)
3. Crea un nuevo Issue con detalles del error

## Próximos Pasos

Después de configurar exitosamente:
1. Explora el código fuente para entender la arquitectura
2. Personaliza el diseño y colores en `ui/theme/`
3. Agrega nuevas funcionalidades según tus necesidades
4. Considera implementar autenticación para múltiples usuarios
5. Agrega tests unitarios y de integración
