
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Read configuration in priority order:
// local.properties -> root .env -> Gradle properties -> environment variables
fun resolveProperty(key: String): String {
    // 1. local.properties
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use { properties.load(it) }

        val value = properties.getProperty(key)
        if (!value.isNullOrBlank()) {
            return value.trim().removeSurrounding("\"")
        }
    }

    // 2. .env in the Android project root
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        val value = envFile.useLines { lines ->
            lines.firstNotNullOfOrNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("$key=")) {
                    trimmed.substringAfter("=")
                        .trim()
                        .removeSurrounding("\"")
                        .takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }
        }

        if (value != null) return value
    }

    // 3. Gradle properties
    if (project.hasProperty(key)) {
        val value = project.property(key)?.toString()
        if (!value.isNullOrBlank()) {
            return value.trim().removeSurrounding("\"")
        }
    }

    // 4. System environment variables
    val envValue = System.getenv(key)
    if (!envValue.isNullOrBlank()) {
        return envValue.trim().removeSurrounding("\"")
    }

    return ""
}

// Escape values safely for generated Java string literals.
fun buildConfigString(value: String): String {
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    return "\"$escaped\""
}

val supabaseUrl = resolveProperty("SUPABASE_URL")
val supabaseAnonKey = resolveProperty("SUPABASE_ANON_KEY")

android {
    namespace = "com.traveldispatcher.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.traveldispatcher.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "SUPABASE_URL",
            buildConfigString(supabaseUrl)
        )

        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            buildConfigString(supabaseAnonKey)
        )
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Debug signing is for testing only.
            // Configure a private release keystore before distributing.
            signingConfig = signingConfigs.getByName("debug")
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
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Jetpack Compose and Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Supabase Auth and Postgrest
    val supabaseVersion = "2.6.1"

    implementation("io.github.jan-tennert.supabase:gotrue-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // Gmail SMTP
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Encrypted preferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
