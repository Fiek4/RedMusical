# Conectar la app con Supabase

## 1. Crear las tablas (una sola vez)

1. En Supabase abre **SQL Editor → New query**.
2. Pega el contenido de [`supabase/migrations/0001_init.sql`](../supabase/migrations/0001_init.sql) y pulsa **Run**.
3. Revisa en **Table Editor** que aparezcan `profiles`, `tracks`, `collab_calls`,
   `applications`, `conversations` y `messages`, y en **Storage** el bucket `audio`.

## 2. Configurar el registro

En **Authentication → Sign In / Providers → Email**:

- Mientras desarrollas, puedes **desactivar "Confirm email"** para que el registro
  inicie sesión de inmediato. Antes de publicar la app, vuelve a activarlo.

## 3. Claves en Android (sin subirlas a GitHub)

La app solo usa la **Project URL** y la **publishable key** (`sb_publishable_...`).
Nunca uses la contraseña de la base de datos ni la `service_role` / `secret` key en la app.

En `local.properties` (ya está en `.gitignore`):

```properties
SUPABASE_URL=https://tkyjrpnfbqpmaabsgops.supabase.co
SUPABASE_KEY=sb_publishable_xxxxxxxx
```

En `app/build.gradle.kts`:

```kotlin
import java.util.Properties

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProps["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProps["SUPABASE_KEY"]}\"")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data-supabase"))
    implementation(project(":presentation"))
}
```

## 4. Leer audios del teléfono

`SupabaseAudioUploader` necesita leer los bytes del archivo que el usuario eligió.
En Android eso se hace con `ContentResolver`:

```kotlin
class AndroidAudioBytesReader(private val context: Context) : AudioBytesReader {
    override suspend fun read(uri: String): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(Uri.parse(uri))!!.use { it.readBytes() }
    }
}
```

## 5. Armar las dependencias (ejemplo con Koin)

```kotlin
val appModule = module {
    single { createMusicSocialClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }
    single<Clock> { Clock.systemUTC() }
    single<IdGenerator> { UuidIdGenerator() }

    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<UserRepository> { SupabaseUserRepository(get()) }
    single<TrackRepository> { SupabaseTrackRepository(get()) }
    single<CollabCallRepository> { SupabaseCollabCallRepository(get()) }
    single<ApplicationRepository> { SupabaseApplicationRepository(get()) }
    single<ChatRepository> { SupabaseChatRepository(get()) }
    single<AudioUploader> { SupabaseAudioUploader(get(), get(), AndroidAudioBytesReader(androidContext())) }

    factory { RegisterUseCase(get()) }
    factory { SaveProfileUseCase(get(), get(), get()) }
    factory { CreateCallUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ApplyToCallUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ReviewApplicationUseCase(get(), get(), get(), get(), get(), get()) }

    viewModel { RegisterViewModel(get()) }
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    // ...el resto de ViewModels igual
}
```

## Seguridad: qué protege la base

La app valida todo antes de enviar, pero cualquiera podría llamar a la API sin la app.
Por eso la base repite las reglas importantes:

- **RLS:** cada usuario solo edita lo suyo; las postulaciones solo las ven quien se postuló
  y el autor de la convocatoria; los mensajes solo los participantes del chat.
- **Triggers:** límite del plan gratis (5 tracks, 2 convocatorias activas), no postularse
  a la propia convocatoria ni a una cerrada.
- **Permisos por columna:** el usuario no puede cambiar su `plan`; el autor solo puede
  cambiar el `status` de una postulación.
- **Storage:** cada usuario solo sube a su carpeta `audio/<su id>/`. La lectura es
  pública con nombres UUID (fácil de reproducir en el reproductor); si más adelante
  queremos demos privadas, se pasa a URLs firmadas.
