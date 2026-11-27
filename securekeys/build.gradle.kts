import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing is optional: present only when secure_keystore.properties exists (not in git).
val signProps = rootProject.file("secure_keystore.properties").takeIf { it.exists() }?.let { f ->
    Properties().apply { f.inputStream().use(::load) }
}

android {
    namespace = "com.dexetra.securekeys"
    // Compose 1.13 / lifecycle 2.11 require compiling against API 37.1+.
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "com.dexetra.securekeys"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "4.0"
    }

    signingConfigs {
        if (signProps != null) {
            create("release") {
                storeFile = rootProject.file(signProps.getProperty("STORE_FILE"))
                storePassword = signProps.getProperty("STORE_PASSWORD")
                keyAlias = signProps.getProperty("KEY_ALIAS")
                keyPassword = signProps.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    buildFeatures {
        compose = true
        // VERSION_NAME for the About section.
        buildConfig = true
    }

    // Emit Java 17 bytecode using whatever JDK runs Gradle (no separate JDK 17 install needed).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.splashscreen)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
