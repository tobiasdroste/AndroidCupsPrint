import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
}

// Apply Detekt to all modules
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true // use the default detekt configuration as a baseline
        allRules = false // activate all available (even unstable) rules
        config.setFrom(files("$rootDir/config/detekt/detekt.yml")) // point to your custom config
        baseline = file("$rootDir/config/detekt/baseline.xml") // a way to suppress issues before introducing detekt
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true) // observe findings in your browser with structure and code snippets
            xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
            txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
            sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        }
    }
}

// Simple helpers to bump versionName minor and versionCode in gradle.properties

val gradlePropsFile = file("$rootDir/gradle.properties")

fun updateGradleProperties(transform: (Properties) -> Unit) {
    val props = Properties()
    gradlePropsFile.inputStream().use { props.load(it) }
    transform(props)
    gradlePropsFile.outputStream().use { out ->
        props.store(out, "Updated by Gradle task")
    }
}

fun bumpMinorVersionString(version: String): String {
    val parts = version.split('.')
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    // Reset patch to 0 when bumping minor
    return "$major.${minor + 1}.0"
}

// Run before creating a release bundle: gradlew bumpVersion
tasks.register("bumpVersion") {
    group = "versioning"
    description =
        "Increment minor version (resets patch to .0) and versionCode (+1) in gradle.properties"
    doLast {
        updateGradleProperties { props ->
            val currentVersion = (props.getProperty("version") ?: "1.0.0").trim()
            val newVersion = bumpMinorVersionString(currentVersion)
            val currentCode = (props.getProperty("versionCode") ?: "1").trim().toIntOrNull() ?: 1
            props.setProperty("version", newVersion)
            props.setProperty("versionCode", (currentCode + 1).toString())
            println("[versioning] version: $currentVersion -> $newVersion")
            println("[versioning] versionCode: $currentCode -> ${currentCode + 1}")
        }
    }
}