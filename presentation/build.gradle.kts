dependencies {
    implementation(project(":domain"))
    // En Android: implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:<versión>")
    implementation(project(":lifecycle-stub"))

    testImplementation(project(":data"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
