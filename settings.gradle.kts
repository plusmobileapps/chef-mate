rootProject.name = "ChefMate"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":client:bottomnav:impl")
include(":client:bottomnav:public")
include(":client:composeApp")
include(":client:database")
include(":client:grocery:impl")
include(":client:grocery:public")
include(":client:recipe:data:impl")
include(":client:recipe:data:public")
include(":client:recipe:data:testing")
include(":client:recipe:list:impl")
include(":client:recipe:list:public")
include(":client:root:impl")
include(":client:root:public")
include(":client:shared")
include(":client:testing")
include(":server")