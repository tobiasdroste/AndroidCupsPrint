import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf("META-INF/LICENSE.txt", "META-INF/NOTICE.txt")
        }
    }

    defaultConfig {
        applicationId = "com.tobiasdroste.papercups"
        minSdk = 23
        targetSdk = 34
        versionCode = 5
        versionName = version as String
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        namespace = "com.tobiasdroste.papercups"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation("javax.jmdns:jmdns:3.4.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Koin DI
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")

    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
