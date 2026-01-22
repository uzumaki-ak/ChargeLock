// Purpose: Defines build configuration and dependencies for the entire project

buildscript {
    extra.apply {
        set("compose_version", "1.5.4")
        set("kotlin_version", "1.9.20")
        set("lifecycle_version", "2.7.0")
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
