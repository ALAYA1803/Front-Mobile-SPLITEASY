plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.spliteasy.spliteasy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.spliteasy.spliteasy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // ====== TUS CONSTANTES ======
        buildConfigField("String", "BASE_URL", "\"https://back-spliteasy.onrender.com/\"")

        // reCAPTCHA WEB (se queda igual, Android no lo usará)
        buildConfigField("String", "RECAPTCHA_SITE_KEY", "\"6Lfqx9wrAAAAAITS3xCtu2B1CXbyM03jc1jAnVTe\"")
        buildConfigField("String", "RECAPTCHA_SECRET_KEY", "\"6LdNpN4rAAAAADl5kXk7WBtnR4I7qXYmKtMWV6OL\"")

        // Play Integrity (USA TU PROJECT NUMBER REAL DE GCP)
        buildConfigField("long", "PROJECT_NUMBER", "197628677861")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "RECAPTCHA_SITE_KEY", "\"6LddQN8rAAAAAPuC7XmDDxbaC7di1ER8T1E5Sy31\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Repite por claridad, mismo endpoint
            buildConfigField("String", "BASE_URL", "\"https://back-spliteasy.onrender.com/api/v1/\"")
            buildConfigField("String", "RECAPTCHA_SITE_KEY", "\"6LddQN8rAAAAAPuC7XmDDxbaC7di1ER8T1E5Sy31\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit + Moshi + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    // Para integrar Tasks<> de Play Services con coroutines:
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // (más estable)

    // Play Integrity
    implementation("com.google.android.play:integrity:1.4.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // reCAPTCHA Android (no la usarás si vas 100% con Integrity, pero puedes dejarla)
    implementation("com.google.android.recaptcha:recaptcha:18.8.0")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
