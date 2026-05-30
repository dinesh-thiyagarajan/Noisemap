pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Noisemap"
include(":app")

// Core Modules
include(":core:core-ui")
include(":core:core-domain")
include(":core:core-data")
include(":core:core-common")

// Feature modules — flat, same level as :app
include(":feature-onboarding")
include(":feature-dashboard")
include(":feature-appdetail")
include(":feature-timeline")
include(":feature-insights")
include(":feature-about")

// Service module — flat, same level as :app
include(":notification-service")
