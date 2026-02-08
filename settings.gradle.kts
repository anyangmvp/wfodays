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
        // 百度地图仓库
        maven { url = uri("https://raw.github.com/baidumapapi/BaiduMap_AndroidSDK/master") }
    }
}

rootProject.name = "WFODays"
include(":app")
