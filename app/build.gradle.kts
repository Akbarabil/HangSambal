plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.hangsambal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hangsambal"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation ("com.github.deano2390:MaterialShowcaseView:1.3.4")
    implementation ("com.airbnb.android:lottie:5.0.3")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.karumi:dexter:6.2.3")

    //play service location untuk depedencies fused location
    implementation ("com.google.android.gms:play-services-location:20.0.0")

    // Mapbox SDK
    implementation("com.mapbox.maps:android:11.1.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:7.4.0")
    // OkHttp untuk request ke Optimization API
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // CameraX
    implementation ("androidx.camera:camera-core:1.4.0")
    implementation ("androidx.camera:camera-camera2:1.4.0")
    implementation ("androidx.camera:camera-lifecycle:1.4.0")
    implementation ("androidx.camera:camera-video:1.4.0")
    implementation ("androidx.camera:camera-view:1.4.0")
    implementation ("androidx.camera:camera-extensions:1.4.0")

    // JSON parser
    implementation ("org.json:json:20240303")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1") // ViewModel
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")  // LiveData

    debugImplementation ("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation ("com.github.chuckerteam.chucker:library-no-op:4.0.0")

    //ImageCompresor
    implementation ("id.zelory:compressor:3.0.1")

    //GLide
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")

}