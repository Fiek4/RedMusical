package com.musicsocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.musicsocial.app.ui.register.RegisterScreen
import com.musicsocial.app.ui.theme.MusicSocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicSocialTheme {
                // Por ahora solo la pantalla de registro, para probar la conexión con Supabase.
                RegisterScreen()
            }
        }
    }
}
