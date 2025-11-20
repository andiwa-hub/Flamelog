plugins {
    id("com.android.application")
    alias(libs.plugins.google.gms.google.services) // Firebase plugin
}

android {
    namespace = "com.example.flamelog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.flamelog"
        minSdk = 24
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.3")
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Firebase BoM (required to sync versions properly)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase SDKs (versions now controlled by BoM)
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}


