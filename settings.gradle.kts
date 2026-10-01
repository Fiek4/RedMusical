pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "musicsocial"
include(":app")
include(":domain")
include(":data")
include(":data-supabase")
include(":presentation")
