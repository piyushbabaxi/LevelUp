plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.piyush.levelup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.piyush.levelup"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
