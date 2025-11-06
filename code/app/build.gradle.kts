plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.example.connect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.connect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Use Java 17 + desugaring (needed for java.time / LocalDate)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    // --- Firebase (use ONE BoM; latest from your friend) ---
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")       // login
    implementation("com.google.firebase:firebase-firestore")  // event list
    // (Optional) analytics, messaging, storage later:
    // implementation("com.google.firebase:firebase-analytics")

    // Desugaring for java.time on older Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // --- UI / AndroidX (keep your catalog) ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")


    // --- Tests ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation ("com.google.firebase:firebase-firestore")
    implementation("com.google.android.material:material:1.9.0")
}
