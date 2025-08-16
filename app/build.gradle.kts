plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.ingredients"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ingredients"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
		viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkDependencies = false
        abortOnError = false
        ignoreTestSources = true
    }
}
dependencies {
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")

    // Kotlin and Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
	
	// CameraX core library using the camera2 implementation
    implementation("androidx.camera:camera-camera2:1.3.1")

    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:1.3.1")

    // CameraX View class
    implementation("androidx.camera:camera-view:1.3.1")

    // CameraX Extensions (optional)
    implementation("androidx.camera:camera-extensions:1.3.1")

    // Kotlin standard library (if not already included)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")

    // JUnit
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
}
