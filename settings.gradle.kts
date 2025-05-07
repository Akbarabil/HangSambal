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
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoiYWJhYmlsYWJpbCIsImEiOiJjbWFhc2JjeGEyMzh4MmpxemQxaXc2YmloIn0.7UPWL6pxrOJ240xnTdrMPg"  // Replace with your correct token
            }
        }
    }
}

rootProject.name = "HangSambal"
include(":app")
 