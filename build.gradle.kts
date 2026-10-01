plugins {
    id("com.android.application") version "8.13.0" apply false
    kotlin("android") version "2.4.0" apply false
    kotlin("plugin.compose") version "2.4.0" apply false
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.serialization") version "2.4.0" apply false
}

// Configuración común para los módulos Kotlin puros (todos menos :app, que es Android).
configure(subprojects.filter { it.name != "app" }) {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
