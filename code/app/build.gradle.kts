import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // THIS LINE KILLS THE FIRESTORE CRASH FOREVER
            all {
                systemProperty ("robolectric.enabledSdks", "28")
                systemProperty ("firebase.firestore.settings.persistenceEnabled", "false")
            }
        }
    }
}

dependencies {

    // ---------- Android libraries ----------
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ---------- Firebase ----------
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-firestore")

    // ---------- Other libraries ----------
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ---------- Google Maps and Location Services (US 02.02.02) ----------
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.core)

    // ---------- Unit testing ----------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("androidx.test:core:1.6.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // ---------- Android Instrumented testing ----------
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("org.mockito:mockito-android:5.12.0")
    androidTestImplementation("org.mockito:mockito-android:5.12.0")
    androidTestImplementation ("org.mockito:mockito-android:5.5.0")

    testImplementation ("com.google.firebase:firebase-appcheck-safetynet:16.1.2") // any recent version
    testImplementation("com.google.firebase:firebase-firestore:24.10.0")
    testImplementation ("org.robolectric:robolectric:4.13")
    testImplementation ("androidx.test:core:1.5.0")
}
