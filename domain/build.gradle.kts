plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
dependencies{
    implementation(libs.kotlinx.coroutines.core)
    // JUnit
    testImplementation(libs.junit)
    // Coroutines Test
    testImplementation(libs.kotlinx.coroutines.test)
    // MockK
    testImplementation(libs.mockk)
}