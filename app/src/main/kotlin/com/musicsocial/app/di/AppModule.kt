package com.musicsocial.app.di

import com.musicsocial.app.AndroidAudioBytesReader
import com.musicsocial.app.BuildConfig
import com.musicsocial.data.location.TextLocationCatalog
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
 * Aquí se decide qué implementación usa la app. Para probar sin internet,
 * cambia los repositorios Supabase por los InMemory del módulo :data.
 */
val appModule = module {
    single { createMusicSocialClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }
    single<Clock> { Clock.systemUTC() }
    single<IdGenerator> { UuidIdGenerator() }

    // Repositorios
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<UserRepository> { SupabaseUserRepository(get()) }
    single<TrackRepository> { SupabaseTrackRepository(get()) }
    single<CollabCallRepository> { SupabaseCollabCallRepository(get()) }
    single<ApplicationRepository> { SupabaseApplicationRepository(get()) }
    single<ChatRepository> { SupabaseChatRepository(get()) }
    single<LocationCatalog> {
        val assets = androidContext().assets
        TextLocationCatalog {
            withContext(Dispatchers.IO) { assets.open("locations.txt").bufferedReader().use { it.readText() } }
        }
    }
    single<AudioUploader> { SupabaseAudioUploader(get(), get(), AndroidAudioBytesReader(androidContext())) }

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
    viewModel { params -> ReviewApplicationsViewModel(params.get(), get(), get()) }
}
