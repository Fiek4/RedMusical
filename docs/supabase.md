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

`app/build.gradle.kts` las lee y las expone como `BuildConfig.SUPABASE_URL` y
`BuildConfig.SUPABASE_KEY`. Si falta alguna, el build se detiene con un mensaje claro.

## 4. Dónde se conecta todo

- `app/.../di/AppModule.kt`: crea el cliente de Supabase y decide qué repositorios usar.
  Para probar sin internet, cambia los `Supabase...Repository` por los `InMemory...` de `:data`.
- `app/.../AndroidAudioBytesReader.kt`: lee los audios del teléfono para subirlos.

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
