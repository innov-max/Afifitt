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
        maven { url  = uri("https://storage.zego.im/maven")}
        maven { url = uri("https://www.jitpack.io") }
        maven {url = uri("https://dl.cloudsmith.io/public/cometchat/cometchat/maven/")}
        maven { url = uri("https://repo.sendbird.com/public/maven")}


    }

// check out the issue with the sendbird Api asap... inhibiting the build
}

rootProject.name = "Afifit"
include(":app")

 
