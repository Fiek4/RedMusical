# MusicSocial · código Kotlin (MVVM)

Capas listas: **modelo, validaciones, repositorios, casos de uso y ViewModels**.
Falta la UI (Jetpack Compose) y el backend real.

## Módulos

```
domain/          Kotlin puro, el corazón de la app
├── model/       Entidades: UserProfile, Track, CollabCall, CollabApplication,
│                Conversation/Message, Collaboration, Follow, enums
├── form/        Lo que el usuario escribe en cada formulario (antes de validar)
├── validation/  Rules, Limits, ValidationError, un validador por formulario
├── repository/  Interfaces de datos (Auth, User, Track, CollabCall, Application, Chat, AudioUploader)
└── usecase/     Register, SaveProfile, CreateCall, ApplyToCall, ReviewApplication

data/            Implementaciones en memoria de los repositorios (para desarrollar y probar
                 sin backend). Luego se agregan las de Supabase/Firebase.

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

33 pruebas: validaciones (20) y ViewModels (13), incluido el flujo completo
"Ana publica → Luis se postula → Ana acepta → se abre el chat".

```
gradle test
```

## Pasar a Android Studio

1. Crear proyecto Android (Empty Compose Activity) y copiar `domain/`, `data/` y `presentation/`
   (o mover `presentation/` dentro de `app/`).
2. Borrar `lifecycle-stub/` y en su lugar usar
   `implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:<versión>")`.
3. Crear los ViewModels con Hilt o Koin (inyección de dependencias), conectando
   las implementaciones en memoria por ahora.
