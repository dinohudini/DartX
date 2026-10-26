import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

android {
    namespace = "com.dinohud.dartx"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dinohud.dartx"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    val releaseStoreFile = signingValue("storeFile", "DARTX_KEYSTORE_FILE")

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = signingValue("storePassword", "DARTX_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "DARTX_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "DARTX_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkDependencies = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.compose.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    debugImplementation(libs.compose.ui.tooling)
}
