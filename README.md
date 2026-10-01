# MusicSocial · código Kotlin (MVVM)

App Android en Kotlin con **MVVM**, **Jetpack Compose**, **Koin** y **Supabase**.
Pantallas listas: inicio de sesión, registro, perfil de artista (onboarding) y feed de convocatorias con filtros.

## Módulos

```
app/             App Android: navegación (navigation/AppNavHost.kt), pantallas Compose (ui/), Koin (di/AppModule.kt),
                 lectura de audios del teléfono y textos en res/values/strings.xml

domain/          Kotlin puro, el corazón de la app
├── model/       Entidades: UserProfile, Track, CollabCall, CollabApplication,
│                Conversation/Message, Collaboration, Follow, enums
├── form/        Lo que el usuario escribe en cada formulario (antes de validar)
├── validation/  Rules, Limits, ValidationError, un validador por formulario
├── repository/  Interfaces de datos (Auth, User, Track, CollabCall, Application, Chat, AudioUploader)
└── usecase/     Register, SaveProfile, CreateCall, ApplyToCall, ReviewApplication

data/            Implementaciones en memoria de los repositorios (para pruebas y desarrollo
                 sin conexión).

data-supabase/   Implementaciones reales con Supabase (auth, base de datos y audios),
                 DTOs y conversión a modelos del dominio.

supabase/        Script SQL: tablas, reglas de seguridad (RLS), triggers y bucket de audio.

presentation/    ViewModels con StateFlow, uno por pantalla:
├── auth/        RegisterViewModel
├── profile/     EditProfileViewModel (onboarding y editar perfil)
├── feed/        FeedViewModel (convocatorias con filtros)
└── call/        CreateCallViewModel, ApplyViewModel, ReviewApplicationsViewModel
```

## Cómo fluye una acción (MVVM)

```
UI (Compose) ──onPublish()──▶ ViewModel ──▶ CreateCallUseCase ──▶ validar ──▶ subir audio ──▶ Repository
     ▲                            │
     └──── uiState (StateFlow) ◀──┘  errores por campo, isLoading, publishedCallId...
```

- **La UI solo hace dos cosas:** dibujar `uiState` y llamar funciones `onAlgo()` del ViewModel.
- **El ViewModel no sabe de base de datos:** recibe casos de uso o interfaces de repositorio.
- **El caso de uso tiene la lógica:** valida, aplica reglas de negocio y guarda.
  Devuelve `UseCaseResult` = `Success`, `Invalid` (errores de validación) o `Failure` (red, permisos...).
- **Validación en vivo:** los errores aparecen después del primer intento de envío y
  luego se recalculan mientras el usuario corrige.
- **Formulario ≠ modelo.** `CollabCallForm` tiene texto libre y nulos; `CollabCall` ya es válido.
- **Los errores no tienen texto.** `ValidationError.TooShort(8)` se traduce en la UI con `strings.xml`.
- La hora (`Clock`) y los ids (`IdGenerator`) se inyectan para poder probar.

## Pruebas

43 pruebas: validaciones (20), ViewModels (19) y conversión de datos de Supabase (4),
incluido el flujo completo
"Ana publica → Luis se postula → Ana acepta → se abre el chat".

```
./gradlew test
```

## Abrir y ejecutar en Android Studio

1. **File → New → Project from Version Control**, pega `https://github.com/Fiek4/RedMusical.git` y pulsa Clone.
2. En la raíz del proyecto, abre (o crea) `local.properties` y agrega tus claves de Supabase:
   ```properties
   SUPABASE_URL=https://tkyjrpnfbqpmaabsgops.supabase.co
   SUPABASE_KEY=sb_publishable_xxxxxxxx
   ```
   Android Studio ya escribe ahí `sdk.dir`; deja esa línea. Este archivo no se sube a GitHub.
3. Espera a que termine el **Gradle Sync** y pulsa **Run ▶** con un emulador o tu teléfono.
4. Crea una cuenta en la app. Si todo va bien, la verás en Supabase → Authentication → Users.

Más detalles de Supabase en [docs/supabase.md](docs/supabase.md).
