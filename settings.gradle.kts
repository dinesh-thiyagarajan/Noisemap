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
    }
}

rootProject.name = "Notifiq"
include(":app")

// Core Modules
include(":core:core-ui")
include(":core:core-domain")
include(":core:core-data")
include(":core:core-common")

// Service Module
include(":service:notification-service")

// Feature Modules
include(":feature:feature-onboarding")
include(":feature:feature-dashboard")
include(":feature:feature-appdetail")
include(":feature:feature-timeline")
include(":feature:feature-insights")
include(":feature:feature-about")
