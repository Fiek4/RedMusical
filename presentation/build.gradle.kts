dependencies {
    api(project(":domain"))
    // ViewModel y viewModelScope de Android. La librería es multiplataforma,
    // por eso este módulo sigue siendo Kotlin puro y se prueba sin emulador.
    api("androidx.lifecycle:lifecycle-viewmodel:2.9.3")

    testImplementation(project(":data"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
