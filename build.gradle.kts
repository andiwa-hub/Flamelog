// 🔹 Project-level build.gradle.kts

plugins {
    // Version catalog is optional, so we explicitly declare these
    id("com.android.application") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
