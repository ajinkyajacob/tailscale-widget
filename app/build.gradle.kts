plugins {
    id("com.android.application")
}

val keystoreFile: String? = System.getenv("ANDROID_KEYSTORE_FILE")
val versionNameOverride: String? = providers.gradleProperty("versionName").orNull
val versionCodeOverride: Int? = providers.gradleProperty("versionCode").orNull?.toInt()

android {
    namespace = "com.ajinkya.tailscalewidget"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ajinkya.tailscalewidget"
        minSdk = 26
        targetSdk = 36
        versionCode = versionCodeOverride ?: 1
        versionName = versionNameOverride ?: "1.0"
    }

    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
