package com.musicsocial.app.di

import com.musicsocial.app.AndroidAudioBytesReader
import com.musicsocial.app.BuildConfig
import com.musicsocial.app.demo.DemoAudioUploader
import com.musicsocial.app.demo.DemoData
import com.musicsocial.data.location.TextLocationCatalog
import com.musicsocial.data.memory.InMemoryApplicationRepository
import com.musicsocial.data.memory.InMemoryAuthRepository
import com.musicsocial.data.memory.InMemoryChatRepository
import com.musicsocial.data.memory.InMemoryCollabCallRepository
import com.musicsocial.data.memory.InMemoryTrackRepository
import com.musicsocial.data.memory.InMemoryUserRepository
import com.musicsocial.data.memory.UuidIdGenerator
import com.musicsocial.data.supabase.SupabaseApplicationRepository
import com.musicsocial.data.supabase.SupabaseAudioUploader
import com.musicsocial.data.supabase.SupabaseAuthRepository
import com.musicsocial.data.supabase.SupabaseChatRepository
import com.musicsocial.data.supabase.SupabaseCollabCallRepository
import com.musicsocial.data.supabase.SupabaseTrackRepository
import com.musicsocial.data.supabase.SupabaseUserRepository
import com.musicsocial.data.supabase.createMusicSocialClient
import com.musicsocial.domain.repository.ApplicationRepository
import com.musicsocial.domain.repository.AudioUploader
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.ChatRepository
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.repository.IdGenerator
import com.musicsocial.domain.repository.LocationCatalog
import com.musicsocial.domain.repository.TrackRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.usecase.ApplyToCallUseCase
import com.musicsocial.domain.usecase.CreateCallUseCase
import com.musicsocial.domain.usecase.GetStartDestinationUseCase
import com.musicsocial.domain.usecase.RegisterUseCase
import com.musicsocial.domain.usecase.ReviewApplicationUseCase
import com.musicsocial.domain.usecase.SaveProfileUseCase
import com.musicsocial.domain.usecase.SignInUseCase
import com.musicsocial.presentation.auth.LoginViewModel
import com.musicsocial.presentation.auth.RegisterViewModel
import com.musicsocial.presentation.call.ApplyViewModel
import com.musicsocial.presentation.call.CreateCallViewModel
import com.musicsocial.presentation.call.ReviewApplicationsViewModel
import com.musicsocial.presentation.feed.FeedViewModel
import com.musicsocial.presentation.profile.EditProfileViewModel
import com.musicsocial.presentation.session.StartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

/**
 * Aquí se decide qué implementación usa la app: Supabase, o datos de ejemplo
 * en memoria si local.properties tiene DEMO_MODE=true (ver [demoModule]).
 */
val appModules get() = listOf(commonModule, if (BuildConfig.DEMO_MODE) demoModule else supabaseModule)

/** Repositorios reales contra Supabase. */
private val supabaseModule = module {
    single { createMusicSocialClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<UserRepository> { SupabaseUserRepository(get()) }
    single<TrackRepository> { SupabaseTrackRepository(get()) }
    single<CollabCallRepository> { SupabaseCollabCallRepository(get()) }
    single<ApplicationRepository> { SupabaseApplicationRepository(get()) }
    single<ChatRepository> { SupabaseChatRepository(get()) }
    single<AudioUploader> { SupabaseAudioUploader(get(), get(), AndroidAudioBytesReader(androidContext())) }
}

/**
 * Modo demo: todo vive en memoria y arranca con artistas, convocatorias y
 * postulantes de ejemplo. Sirve para ver y probar pantallas sin tocar Supabase.
 * Los datos se pierden al cerrar la app.
 */
private val demoModule = module {
    single { InMemoryAuthRepository(get()) }
    single<AuthRepository> { get<InMemoryAuthRepository>() }
    single<UserRepository> { InMemoryUserRepository() }
    single<TrackRepository> { InMemoryTrackRepository() }
    single<CollabCallRepository> { InMemoryCollabCallRepository() }
    single<ApplicationRepository> { InMemoryApplicationRepository() }
    single<ChatRepository> { InMemoryChatRepository() }
    single<AudioUploader> { DemoAudioUploader() }
    single { DemoData(androidContext(), get(), get(), get(), get(), get()) }
}

private val commonModule = module {
    single<Clock> { Clock.systemUTC() }
    single<IdGenerator> { UuidIdGenerator() }
    single<LocationCatalog> {
        val assets = androidContext().assets
        TextLocationCatalog {
            withContext(Dispatchers.IO) { assets.open("locations.txt").bufferedReader().use { it.readText() } }
        }
    }

    // Casos de uso
    factory { RegisterUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { GetStartDestinationUseCase(get(), get()) }
    factory { SaveProfileUseCase(get(), get(), get()) }
    factory { CreateCallUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ApplyToCallUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ReviewApplicationUseCase(get(), get(), get(), get(), get(), get()) }

    // ViewModels (los que reciben un id lo toman como parámetro)
    viewModel { StartViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { EditProfileViewModel(get(), get(), get(), get()) }
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { CreateCallViewModel(get(), get()) }
    viewModel { params -> ApplyViewModel(params.get(), get(), get(), get(), get()) }
    viewModel { params -> ReviewApplicationsViewModel(params.get(), get(), get(), get()) }
}
