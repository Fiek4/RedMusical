# MusicSocial · Definición del MVP

## 1. La apuesta del MVP

Una sola promesa: **"Encuentra con quién terminar tu próximo tema."**

El MVP no intenta reemplazar a SoundCloud ni a BeatStars. Su núcleo es el ciclo de colaboración:

> Publico lo que busco → alguien se postula con audio → hablamos → hacemos el tema.

Si ese ciclo funciona y la gente vuelve, el resto (venta de beats, streaming, estadísticas) se agrega después.

**Métrica principal:** número de colaboraciones que llegan al chat (postulación aceptada) por semana.

---

## 2. Usuarios del MVP

Dos roles, pero una sola cuenta (un usuario puede ser ambos):

| Rol | Qué quiere | Ejemplo |
|---|---|---|
| **Creador** | Encontrar a alguien que complete su idea | Productor con un beat que busca vocalista |
| **Talento** | Encontrar proyectos donde aportar | Cantante, rapero, músico, mezclador |

Al registrarse, el usuario elige sus **roles** (productor, vocalista, rapero, instrumentista, ingeniero de mezcla, compositor) y sus **géneros**.

---

## 3. Pantallas (8 en total)

### 3.1 Registro y onboarding
- Registro con correo, Google o Apple.
- 3 pasos: nombre artístico y foto → roles y géneros → ciudad/país (opcional, para filtros locales).
- Al final: "Sube tu primer track" (se puede saltar, pero se recomienda).

### 3.2 Inicio (feed de convocatorias)
- Lista de **convocatorias** recientes, filtradas por los roles y géneros del usuario.
- Cada tarjeta: foto y nombre del autor, qué busca ("Busco vocalista R&B"), género, BPM, preview de audio de 30 s con botón play, fecha límite, número de postulantes.
- Filtros simples: rol buscado, género, ubicación (cerca / mi país / todo el mundo).
- Botón flotante: **"+ Crear convocatoria"**.

### 3.3 Detalle de convocatoria
- Audio de referencia completo (beat o demo).
- Descripción: qué busca, estilo de referencia, plazo, tipo de acuerdo (ver 4.3).
- Botón **"Postularme"**.
- Si es mía: lista de postulantes.

### 3.4 Crear convocatoria
- Campos: título, rol buscado, género, BPM y tonalidad (opcionales), descripción, audio (obligatorio, máx. 5 min), fecha límite, tipo de acuerdo.
- Límite en el plan gratis: 2 convocatorias activas a la vez.

### 3.5 Postularse
- Grabar o subir una **demo corta** (máx. 60 s) sobre el beat o con material propio.
- Mensaje de texto breve.
- Una postulación por convocatoria.

### 3.6 Revisar postulantes (para el autor)
- Lista de postulantes con su demo para escuchar seguido (tipo playlist).
- Acciones: **Aceptar** (abre chat) / **Descartar** (se le notifica con amabilidad).
- Se puede aceptar a más de uno.

### 3.7 Chat
- Chat 1 a 1 que solo se abre cuando se acepta una postulación (evita spam).
- Texto, notas de voz y envío de archivos de audio.
- Botón "Marcar colaboración como terminada" → ambos confirman → aparece en sus perfiles como **colaboración completada**.

### 3.8 Perfil / blog del artista
- Foto, nombre artístico, roles, géneros, ciudad, bio, enlaces externos (Spotify, Instagram, YouTube).
- **Tracks destacados** (hasta 5 en el plan gratis) con reproductor.
- **Colaboraciones completadas** (insignia social: es la prueba de que el artista cumple).
- Convocatorias activas del artista.
- Botón seguir.

---

## 4. Funcionalidades clave y decisiones

### 4.1 Reproductor
- Reproductor mínimo que siga sonando al navegar entre pantallas (mini-player abajo).
- Sin descargas en el MVP: solo streaming.

### 4.2 Notificaciones
- Alguien se postuló a tu convocatoria.
- Te aceptaron / descartaron.
- Nuevo mensaje en chat.
- Nueva convocatoria que encaja con tus roles (máx. 1 al día para no cansar).

### 4.3 Tipo de acuerdo (simple)
Un selector en la convocatoria, sin contratos todavía:
- **Colaboración libre** (reparto de regalías a acordar).
- **Pago** (rango de precio visible).
- **Intercambio** (yo te ayudo, tú me ayudas).

Los contratos y splits firmados dentro de la app quedan para la versión 2.

### 4.4 Confianza y moderación
- Botón de reportar en perfiles, convocatorias y audios.
- Aceptar términos que prohíben subir música ajena.
- Verificación de correo obligatoria.
- Bloquear usuarios.

---

## 5. Lo que NO entra en el MVP

| Fuera del MVP | Por qué | Cuándo |
|---|---|---|
| Venta de beats / licencias | Requiere pagos, impuestos, licencias: mucho trabajo | v2 |
| Splits y contratos firmados | Complejo legalmente | v2 |
| Feed social tipo Instagram (likes, comentarios en posts) | Distrae del núcleo | v2 |
| Match tipo Tinder | Buena idea, pero se prueba cuando haya masa de usuarios | v2 |
| Editor de audio / DAW | Muy caro de construir; BandLab ya lo hace | Quizás nunca |
| Estadísticas avanzadas | Va en el plan Pro | v2 |

---

## 6. Flujo principal (de punta a punta)

1. Ana (productora) se registra, sube 2 beats a su perfil.
2. Crea convocatoria: "Busco rapero para beat de drill, 140 BPM, fecha límite 15 días".
3. Luis ve la convocatoria en su feed (es rapero, le gusta el drill), escucha el beat y graba 45 s encima. Se postula.
4. Ana recibe notificación, escucha 8 demos seguidas y acepta a Luis.
5. Se abre el chat; se pasan archivos y notas de voz.
6. Terminan el tema y marcan la colaboración como completada.
7. Ambos perfiles muestran la colaboración. Luis gana visibilidad y credibilidad.

---

## 7. Stack técnico sugerido

Pensado para una persona o un equipo pequeño, rápido de construir:

- **App móvil:** React Native con Expo (iOS y Android con un solo código) o Flutter.
- **Backend y base de datos:** Supabase (auth, base de datos Postgres, almacenamiento, tiempo real para el chat) o Firebase.
- **Audio:** almacenamiento en Supabase Storage o Cloudflare R2 (más barato para archivos grandes); convertir los audios a MP3/AAC de calidad media al subirlos para ahorrar espacio.
- **Notificaciones push:** Expo Notifications o Firebase Cloud Messaging.

Límites para controlar costos: archivos de máx. 20 MB, demos de máx. 60 s, tracks de máx. 5 min.

---

## 8. Modelo de datos (simplificado)

- **Usuario**: id, nombre artístico, foto, bio, roles[], géneros[], ciudad, enlaces.
- **Track**: id, usuario, título, audio, género, duración.
- **Convocatoria**: id, autor, título, rol buscado, género, BPM, tonalidad, descripción, audio, fecha límite, tipo de acuerdo, estado (abierta / cerrada).
- **Postulación**: id, convocatoria, postulante, demo, mensaje, estado (pendiente / aceptada / descartada).
- **Chat / Mensaje**: id, participantes, contenido (texto, audio, archivo).
- **Colaboración**: id, convocatoria, participantes, confirmada por ambos.
- **Seguimiento**: seguidor, seguido.

---

## 9. Plan de lanzamiento

1. **Elegir un nicho**: un género y una zona (por ejemplo, música urbana en tu país).
2. **Lista de espera** antes de lanzar: landing page simple + Instagram/TikTok mostrando el concepto.
3. **Sembrar convocatorias**: conseguir 20 a 30 productores conocidos de la escena que publiquen convocatorias el primer día, para que la app no se vea vacía.
4. **Beta cerrada** con 100 a 200 artistas, medir cuántas postulaciones terminan en chat y en colaboración.
5. Ajustar y abrir.

---

## 10. Preguntas abiertas para decidir

- ¿Nicho inicial: qué género y qué país o ciudad?
- ¿React Native o Flutter? (depende de qué conozcas)
- ¿Lo construyes tú, con un equipo, o buscas un socio técnico?
- ¿Nombre de la app?
