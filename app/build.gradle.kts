import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") version "4.4.4"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    namespace = "com.example.music"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.music"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
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


    implementation("androidx.compose.runtime:runtime:1.7.6")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation(libs.androidx.compose.foundation) // corregido aquí
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.navigation.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation(libs.androidx.media3.exoplayer)
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    implementation("androidx.media:media:1.6.0")

    implementation(libs.androidx.appcompat)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    //implementation(libs.com.github.yarolegovich.slidingrootnav)
    implementation("com.github.yarolegovich:SlidingRootNav:v1.1.1")

    implementation("com.github.markomilos:paginate:1.0.0")
    implementation("io.github.everythingme:overscroll-decor-android:1.1.1")

    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.jthink:jaudiotagger:3.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)




}
