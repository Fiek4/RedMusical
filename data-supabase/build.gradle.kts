plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":domain"))

    implementation(platform("io.github.jan-tennert.supabase:bom:3.8.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    // Motor HTTP: OkHttp funciona igual en Android y en la JVM.
    implementation("io.ktor:ktor-client-okhttp:3.5.1")

    testImplementation(kotlin("test"))
}
