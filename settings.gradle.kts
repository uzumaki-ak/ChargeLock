// File: settings.gradle.kts
// Purpose: Configures the Gradle project structure and defines which modules to include
// This is the entry point for Gradle project configuration

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HoldOn"
include(":app")
