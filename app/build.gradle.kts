import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("androidx.room")
}

android {
    compileSdk = 36

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
        targetSdk = 36
        versionCode = 11
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


    room {
        schemaDirectory("$projectDir/schemas")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JVM_17
    }
}

dependencies {
    implementation("javax.jmdns:jmdns:3.4.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Koin DI
    implementation("io.insert-koin:koin-android:4.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")

    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-android-compiler:2.57")

    val roomVersion = "2.7.2"

    implementation("androidx.room:room-runtime:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
