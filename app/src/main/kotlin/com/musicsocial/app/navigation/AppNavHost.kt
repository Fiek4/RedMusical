package com.musicsocial.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.musicsocial.app.ui.call.CallDetailScreen
import com.musicsocial.app.ui.call.CreateCallScreen
import com.musicsocial.app.ui.feed.FeedScreen
import com.musicsocial.app.ui.login.LoginScreen
import com.musicsocial.app.ui.profile.ProfileScreen
import com.musicsocial.app.ui.register.RegisterScreen
import com.musicsocial.app.ui.start.StartScreen
import com.musicsocial.domain.usecase.StartDestination
import kotlinx.serialization.Serializable

/*
 * Rutas de la app. Cada una es un objeto @Serializable: si una pantalla
 * necesita datos (por ejemplo el id de una convocatoria) será una data class.
 */
@Serializable object StartRoute
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object OnboardingRoute
@Serializable object FeedRoute
@Serializable object CreateCallRoute
@Serializable data class CallDetailRoute(val callId: String)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = StartRoute) {

        composable<StartRoute> {
            StartScreen(onDestination = { destination ->
                val route: Any = when (destination) {
                    StartDestination.LOGIN -> LoginRoute
                    StartDestination.ONBOARDING -> OnboardingRoute
                    StartDestination.HOME -> FeedRoute
                }
                navController.clearStackAndGo(route)
            })
        }

        composable<LoginRoute> {
            LoginScreen(
                // Después de entrar volvemos a Start: decide si falta el perfil o va al feed.
                onSignedIn = { navController.clearStackAndGo(StartRoute) },
                onGoToRegister = { navController.navigate(RegisterRoute) },
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onRegistered = { navController.clearStackAndGo(OnboardingRoute) },
                onGoToLogin = { navController.popBackStack() },
            )
        }

        composable<OnboardingRoute> {
            ProfileScreen(onSaved = { navController.clearStackAndGo(FeedRoute) })
        }

        composable<FeedRoute> {
            FeedScreen(
                onSignedOut = { navController.clearStackAndGo(LoginRoute) },
                onCreateCall = { navController.navigate(CreateCallRoute) },
                onOpenCall = { callId -> navController.navigate(CallDetailRoute(callId)) },
            )
        }

        composable<CallDetailRoute> { entry ->
            CallDetailScreen(
                callId = entry.toRoute<CallDetailRoute>().callId,
                onBack = { navController.popBackStack() },
            )
        }

        composable<CreateCallRoute> {
            // Al publicar volvemos al feed, que se recarga solo con la convocatoria nueva.
            CreateCallScreen(
                onPublished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}

/** Navega y borra el historial, para que "atrás" no vuelva al login o al registro. */
private fun NavHostController.clearStackAndGo(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
