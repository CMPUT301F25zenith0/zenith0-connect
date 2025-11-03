plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")  // ⬅️ add this
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

    compileOptions {
        // Needed for java.time (LocalDate) used in Event.java
        sourceCompatibility = JavaVersion.VERSION_17        // ⬅️ change to 17
        targetCompatibility = JavaVersion.VERSION_17        // ⬅️ change to 17
        isCoreLibraryDesugaringEnabled = true               // ⬅️ add this
    }
}

dependencies {
    // 🔥 Firebase (BOM manages versions)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore")

    // java.time support on older Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // your existing deps
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
