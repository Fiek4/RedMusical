import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    // Para las rutas de navegación con tipos (@Serializable).
    kotlin("plugin.serialization")
}

// Claves de Supabase desde local.properties (ese archivo NO se sube a GitHub).
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

fun localProp(name: String): String =
    localProps.getProperty(name)
        ?: error("Falta $name en local.properties. Mira docs/supabase.md, paso 3.")

android {
    namespace = "com.musicsocial.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.musicsocial.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "SUPABASE_URL", "\"${localProp("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProp("SUPABASE_KEY")}\"")
        // DEMO_MODE=true en local.properties: datos de ejemplo en memoria, sin Supabase.
        buildConfigField("boolean", "DEMO_MODE", (localProps.getProperty("DEMO_MODE")?.trim() == "true").toString())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,io.netty.versions.properties}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":data-supabase"))
    implementation(project(":presentation"))

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.navigation:navigation-compose:2.9.3")

    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Inyección de dependencias
    implementation("io.insert-koin:koin-android:4.1.0")
    implementation("io.insert-koin:koin-androidx-compose:4.1.0")
}
