package com.musicsocial.app.demo

import android.content.Context
import android.net.Uri
import com.musicsocial.data.memory.InMemoryAuthRepository
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat
import com.musicsocial.domain.model.Budget
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.domain.repository.ApplicationRepository
import com.musicsocial.domain.repository.AudioUploader
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.repository.UserRepository
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Clock
import java.time.Duration
import kotlin.math.PI
import kotlin.math.sin

/**
 * Datos de ejemplo del modo demo. Entras como "Fer Beats", que ya tiene una
 * convocatoria con 3 postulantes; otros 3 artistas publicaron las suyas.
 * Cuentas: cualquiera de los correos de abajo con la contraseña "demo1234".
 */
class DemoData(
    private val context: Context,
    private val auth: InMemoryAuthRepository,
    private val users: UserRepository,
    private val calls: CollabCallRepository,
    private val applications: ApplicationRepository,
    private val clock: Clock,
) {
    suspend fun seed() {
        val now = clock.instant()
        fun daysAgo(d: Long) = now.minus(Duration.ofDays(d))
        fun inDays(d: Long) = now.plus(Duration.ofDays(d))

        suspend fun artist(email: String, username: String, name: String, roles: Set<ArtistRole>, genres: Set<Genre>, city: String, bio: String): String {
            val id = auth.register(email, PASSWORD)
            users.saveProfile(
                UserProfile(
                    id = id, username = username, artistName = name, bio = bio,
                    roles = roles, genres = genres, city = city, country = "CL", createdAt = daysAgo(30),
                ),
            )
            return id
        }

        val ana = artist("ana@demo.test", "anavox", "Ana Vox", setOf(ArtistRole.SINGER, ArtistRole.SONGWRITER), setOf(Genre.RNB, Genre.POP), "Santiago", "Voz soul y R&B, escribo mis letras.")
        val luis = artist("luis@demo.test", "luismc", "Luis MC", setOf(ArtistRole.RAPPER), setOf(Genre.DRILL, Genre.TRAP), "Puente Alto", "Rapero de drill, 5 años en la escena.")
        val kami = artist("kami@demo.test", "kamiflow", "Kami Flow", setOf(ArtistRole.RAPPER, ArtistRole.SINGER), setOf(Genre.TRAP, Genre.REGGAETON), "Valparaíso", "Flow melódico, trap y reggaetón.")
        val sofi = artist("sofi@demo.test", "sofikeys", "Sofi Keys", setOf(ArtistRole.INSTRUMENTALIST), setOf(Genre.POP, Genre.ROCK), "Viña del Mar", "Piano y teclados en vivo y estudio.")
        val diego = artist("diego@demo.test", "dieguitomix", "Dieguito Mix", setOf(ArtistRole.MIX_ENGINEER), setOf(Genre.TRAP, Genre.HIP_HOP), "Santiago", "Mezcla y master de urbano.")
        val me = artist("fer@demo.test", "ferbeats", "Fer Beats", setOf(ArtistRole.PRODUCER), setOf(Genre.TRAP, Genre.DRILL, Genre.RNB), "Santiago", "Productor de trap y drill.")

        val myCallId = "demo-call-fer"
        calls.createCall(
            CollabCall(
                id = myCallId, authorId = me, title = "Busco rapero para beat de drill",
                lookingFor = ArtistRole.RAPPER, genre = Genre.DRILL,
                description = "Beat oscuro de drill a 140 BPM. Busco flow agresivo y letra sobre la calle. Si sale bien, lo lanzamos juntos en Spotify.",
                referenceAudio = tone("ref-fer", 220.0, 20), bpm = 140, musicalKey = "Fm",
                deadline = inDays(15), dealType = DealType.FREE_COLLAB, createdAt = daysAgo(3),
            ),
        )
        calls.createCall(
            CollabCall(
                id = "demo-call-ana", authorId = ana, title = "Necesito productor para tema R&B",
                lookingFor = ArtistRole.PRODUCER, genre = Genre.RNB,
                description = "Tengo letra y melodía de un tema R&B lento. Busco productor que arme el beat y la producción completa.",
                referenceAudio = tone("ref-ana", 330.0, 25), bpm = 72, musicalKey = "Am",
                deadline = inDays(20), dealType = DealType.PAID, budget = Budget(10_000, 30_000, "USD"), createdAt = daysAgo(1),
            ),
        )
        calls.createCall(
            CollabCall(
                id = "demo-call-diego", authorId = diego, title = "Mezclo tu tema de trap a cambio de beats",
                lookingFor = ArtistRole.PRODUCER, genre = Genre.TRAP,
                description = "Ingeniero de mezcla busca productor: yo mezclo y masterizo tu tema, tú me pasas dos beats.",
                referenceAudio = tone("ref-diego", 262.0, 15), deadline = inDays(30),
                dealType = DealType.EXCHANGE, createdAt = daysAgo(2),
            ),
        )
        calls.createCall(
            CollabCall(
                id = "demo-call-sofi", authorId = sofi, title = "Busco cantante para balada pop",
                lookingFor = ArtistRole.SINGER, genre = Genre.POP,
                description = "Compuse una balada al piano y necesito una voz femenina o masculina para grabar la demo.",
                referenceAudio = tone("ref-sofi", 392.0, 30), bpm = 80, musicalKey = "C",
                deadline = inDays(10), dealType = DealType.FREE_COLLAB, createdAt = daysAgo(5),
            ),
        )

        listOf(
            Triple(luis, "Me encantó el beat, te mando un verso de 45 s. Tengo más letra si te gusta.", 45),
            Triple(kami, "Le metí flow melódico en el coro, escúchalo hasta el final.", 38),
            Triple(ana, "No soy rapera, pero te propongo un hook cantado para el coro.", 30),
        ).forEachIndexed { i, (applicant, message, seconds) ->
            applications.apply(
                CollabApplication(
                    id = "demo-app-$i", callId = myCallId, applicantId = applicant,
                    demo = tone("demo-$i", 180.0 + 60 * i, seconds), message = message,
                    createdAt = daysAgo(2).plus(Duration.ofHours(i * 5L)),
                ),
            )
        }

        auth.signInAs(me)
    }

    /** Genera un tono suave en un .wav para que el reproductor tenga algo que sonar. */
    private fun tone(name: String, hz: Double, seconds: Int): AudioFile {
        val file = File(context.cacheDir, "$name.wav")
        if (!file.exists()) file.writeBytes(sineWav(hz, seconds))
        return AudioFile(Uri.fromFile(file).toString(), AudioFormat.WAV, file.length(), seconds)
    }

    private companion object {
        const val PASSWORD = "demo1234"
        const val RATE = 8_000

        fun sineWav(hz: Double, seconds: Int): ByteArray {
            val samples = RATE * seconds
            val data = ByteBuffer.allocate(samples * 2).order(ByteOrder.LITTLE_ENDIAN)
            repeat(samples) { i -> data.putShort((sin(2 * PI * hz * i / RATE) * 6_000).toInt().toShort()) }
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray()); putInt(36 + samples * 2); put("WAVE".toByteArray())
                put("fmt ".toByteArray()); putInt(16); putShort(1); putShort(1); putInt(RATE); putInt(RATE * 2)
                putShort(2); putShort(16)
                put("data".toByteArray()); putInt(samples * 2)
            }
            return header.array() + data.array()
        }
    }
}

/** En modo demo no se sube nada: el audio queda en el teléfono y se reproduce desde ahí. */
class DemoAudioUploader : AudioUploader {
    override suspend fun upload(audio: AudioFile) = audio
}
