plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.uvers.TplApplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.uvers.TplApplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core library for Android KTX
    implementation("androidx.core:core-ktx:1.13.1")

    // Lifecycle library for managing UI lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Compose Activity library for Jetpack Compose support in Activities
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose BOM for managing versions of Compose libraries
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // Core Compose UI library
    implementation("androidx.compose.ui:ui:1.3.0")

    // Compose UI graphics library
    implementation("androidx.compose.ui:ui-graphics")

    // Tooling support for Compose UI previews
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material Design 3 for Compose
    implementation("androidx.compose.material3:material3")

    // AppCompat for backward compatibility
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Components library
    implementation("com.google.android.material:material:1.12.0")

    // ConstraintLayout for complex layouts
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation component for Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")

    // Unit testing library
    testImplementation("junit:junit:4.13.2")

    // AndroidX JUnit extension for instrumentation tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // Espresso library for UI testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose UI testing library
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.0")

    // Debugging tools for Compose UI
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Manifest tool for Compose UI tests
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-database:20.0.5")
    implementation("com.google.firebase:firebase-storage:20.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.3.0")

    implementation("io.coil-kt:coil-compose:2.0.0")

}
