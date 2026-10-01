# MusicSocial · código Kotlin (MVVM)

Capas listas: **modelo, validaciones, repositorios, casos de uso, ViewModels y conexión con Supabase**.
Falta la UI (Jetpack Compose). Para configurar Supabase sigue [docs/supabase.md](docs/supabase.md).

## Módulos

```
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

lifecycle-stub/  Imitación de androidx.lifecycle.ViewModel SOLO para compilar fuera de
                 Android. En Android Studio se borra y se usa la librería real.
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

37 pruebas: validaciones (20), ViewModels (13) y conversión de datos de Supabase (4),
incluido el flujo completo
"Ana publica → Luis se postula → Ana acepta → se abre el chat".

```
gradle test
```

## Pasar a Android Studio

1. Crear proyecto Android (Empty Compose Activity) y copiar `domain/`, `data/`, `data-supabase/`
   y `presentation/` (o mover `presentation/` dentro de `app/`). Usar Kotlin 2.4 o superior.
2. Borrar `lifecycle-stub/` y en su lugar usar
   `implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:<versión>")`.
3. Configurar Supabase y la inyección de dependencias como explica [docs/supabase.md](docs/supabase.md).
