import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
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

    // Configure Android Lint
    lint {
        abortOnError = false // Don't fail the build if there are Lint errors
        checkReleaseBuilds = true // Check lint on release builds
        checkDependencies = true // Check dependencies for issues
        checkAllWarnings = true // Check all warnings, not just the important ones
        warningsAsErrors = false // Treat all warnings as errors
        baseline = file("lint-baseline.xml") // Baseline file to suppress issues

        // Disable specific Lint checks that might be too strict initially
        disable += setOf(
            "InvalidPackage", // Some libraries have invalid packages
            "ObsoleteSdkInt", // We might need to support older devices
            "GradleDependency" // We'll handle dependency updates separately
        )
    }

    defaultConfig {
        applicationId = "com.tobiasdroste.papercups"
        minSdk = 23
        targetSdk = 36
        versionCode = 13
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
    implementation(libs.jmdns)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)

    androidTestImplementation(libs.espresso.core) {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.truth)
    androidTestImplementation(libs.junit)
    implementation(libs.kotlin.stdlib)
    implementation(libs.timber)

    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
