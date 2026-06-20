import java.net.URL
import java.net.HttpURLConnection
import java.io.File
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

val defaultReleaseKeystorePath =
  rootProject.layout.projectDirectory.file("my-upload-key.jks").asFile.absolutePath

val versionPropertiesFile = rootProject.layout.projectDirectory.file("version.properties").asFile
val versionProperties = Properties().apply {
  if (versionPropertiesFile.isFile) {
    versionPropertiesFile.inputStream().use { load(it) }
  }
}

fun readVersionProperty(name: String, fallback: String): String =
  versionProperties.getProperty(name) ?: fallback

val appVersionMajor = readVersionProperty("versionMajor", "1")
val appVersionMinor = readVersionProperty("versionMinor", "0")
val appVersionPatch = readVersionProperty("versionPatch", "0")
val appVersionCode = readVersionProperty("versionCode", "1").toInt()
val appVersionName = "$appVersionMajor.$appVersionMinor.$appVersionPatch"

val releaseSigningConfigured = run {
  val keystorePath = System.getenv("RELEASE_KEY_FILE") ?: defaultReleaseKeystorePath
  File(keystorePath).isFile &&
    !System.getenv("RELEASE_STORE_PASSWORD").isNullOrBlank() &&
    !System.getenv("RELEASE_KEY_PASSWORD").isNullOrBlank()
}

android {
  namespace = "ir.m4tinbeigi.taskreminder"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "ir.m4tinbeigi.taskreminder"
    minSdk = 24
    targetSdk = 36
    versionCode = appVersionCode
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("RELEASE_KEY_FILE") ?: defaultReleaseKeystorePath
      storeFile = file(keystorePath)
      storePassword = System.getenv("RELEASE_STORE_PASSWORD")
      keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isDebuggable = false
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = if (releaseSigningConfigured) {
        signingConfigs.getByName("release")
      } else {
        null
      }
    }
    debug {
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text.google.fonts)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("downloadVazirFont") {
    val destDir = layout.projectDirectory.dir("src/main/res/font")
    doLast {
        val dir = destDir.asFile
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fonts = mapOf(
            "vazirmatn_regular.ttf" to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Regular.ttf",
            "vazirmatn_bold.ttf" to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Bold.ttf",
            "vazirmatn_light.ttf" to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Light.ttf",
            "vazirmatn_medium.ttf" to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Medium.ttf"
        )
        fonts.forEach { (name, urlStr) ->
            val destFile = File(dir, name)
            if (!destFile.exists()) {
                println("Downloading $name...")
                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 15000
                    conn.inputStream.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Downloaded $name successfully.")
                } catch (e: Exception) {
                    println("Failed to download $name: ${e.message}")
                }
            } else {
                println("$name already exists.")
            }
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("downloadVazirFont")
}
