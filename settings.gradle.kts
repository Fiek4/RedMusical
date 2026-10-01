pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "musicsocial"
include(":domain")
include(":data")
include(":presentation")
include(":lifecycle-stub")
