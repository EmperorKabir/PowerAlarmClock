@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Power Alarm Clocks"

include(":app")

// core
include(
    ":core:domain",
    ":core:data",
    ":core:settings",
    ":core:ui",
    ":core:scheduler",
    ":core:audio",
    ":core:permissions",
    ":core:logging",
)

// features
include(
    ":feature:alarm-list",
    ":feature:alarm-edit",
    ":feature:ringer",
    ":feature:settings",
    ":feature:themes",
    ":feature:statistics",
    ":feature:profiles",
    ":feature:wear",
)

// integrations
include(
    ":integrations:spotify",
    ":integrations:drive",
    ":integrations:fitbit",
    ":integrations:weather",
    ":integrations:airquality",
    ":integrations:traffic",
    ":integrations:tfl",
    ":integrations:calendar",
    ":integrations:holidays",
    ":integrations:tasker",
    ":integrations:cast",
    ":integrations:emergency",
    ":integrations:smarthome",
    ":integrations:nfc",
    ":integrations:rest-api",
)
