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

        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }

        maven {
            credentials {
                username = "5ec8b805723771c6a5f2c278"
                password = "8=iAa-1s1z82"
            }
            url = uri( "https://packages.aliyun.com/5ec8b82d405cdab50f3fd98e/maven/repo-jgbgd")
        }
    }
}

rootProject.name = "SocketUtil"
include(":app")
include(":udSocket")

