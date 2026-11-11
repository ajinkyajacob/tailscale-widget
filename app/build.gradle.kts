plugins {
    id("com.android.application")
}

android {
    namespace = "com.ajinkya.tailscalewidget"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ajinkya.tailscalewidget"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
