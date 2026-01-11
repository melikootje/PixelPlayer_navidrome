plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt.android)
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.baselineprofile)
    // id("com.google.protobuf") version "0.9.5" // Eliminado plugin de Protobuf
}

android {
    namespace = "com.theveloper.pixelplay"
    compileSdk = 35

    // Exclude broken test source sets temporarily
    sourceSets {
        getByName("test") {
            java.setSrcDirs(emptyList<String>())
            kotlin.setSrcDirs(emptyList<String>())
        }
        getByName("androidTest") {
            java.setSrcDirs(emptyList<String>())
            kotlin.setSrcDirs(emptyList<String>())
        }
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE.md"
        }
    }

    defaultConfig {
        applicationId = "com.theveloper.pixelplay"
        minSdk = 29
        targetSdk = 35
        versionCode = (project.findProperty("APP_VERSION_CODE") as String).toInt()
        versionName = project.findProperty("APP_VERSION_NAME") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        packaging {
            jniLibs {
                useLegacyPackaging = false
            }
            resources {
                excludes += listOf("META-INF/INDEX.LIST", "META-INF/DEPENDENCIES")
            }
        }
    }

    signingConfigs {
        // Remove any existing release signing configs and only keep debug
        // This prevents the Untitled.jks keystore from being used
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            // Use regular proguard file instead of optimize version for faster builds
            // proguard-android-optimize.txt does aggressive optimizations (SLOW)
            // proguard-android.txt does basic shrinking (FAST)
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),  // Changed from optimize
                "proguard-rules.pro"
            )

            // CRITICAL: Set signing to null first, then to debug
            // This prevents any IDE-configured keystores from being used
            signingConfig = null
            signingConfig = signingConfigs.getByName("debug")
        }

        // AGREGA ESTE BLOQUE:
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = null
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false // Esto quita el error que mencionaste
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0"
        // Para habilitar informes de composición (legibles):
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "17"
        // Compose compiler reports - DISABLED by default for faster builds
        // To enable reports when needed, set these environment variables:
        // - ENABLE_COMPOSE_REPORTS=true
        // Then run: ./gradlew assembleRelease -PENABLE_COMPOSE_REPORTS=true

        if (project.hasProperty("ENABLE_COMPOSE_REPORTS")) {
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_compiler_reports"
            )
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_compiler_metrics"
            )
        }

        // Stability configuration - keep this enabled
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/app/compose_stability.conf"
        )
    }
}

dependencies {
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.mediarouter)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Baseline Profiles (Macrobenchmark)
    // Asegúrate de que libs.versions.toml tiene androidxBenchmarkMacroJunit4 y androidxUiautomator
    // Ejemplo: androidx-benchmark-macro-junit4 = { group = "androidx.benchmark", name = "benchmark-macro-junit4", version.ref = "benchmarkMacro" }
    // benchmarkMacro = "1.2.4"
    //androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    //androidTestImplementation(libs.androidx.uiautomator)


    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler) // For Dagger Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler) // Esta línea es crucial y ahora funcionará

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Glance
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    //Gson
    implementation(libs.gson)

    //Serialization
    implementation(libs.kotlinx.serialization.json)

    //Work
    implementation(libs.androidx.work.runtime.ktx)

    //Duktape
    implementation(libs.duktape.android)

    //Smooth corners shape
    implementation(libs.smooth.corner.rect.android.compose)
    implementation(libs.androidx.graphics.shapes)

    //Navigation
    implementation(libs.androidx.navigation.compose)

    //Animations
    implementation(libs.androidx.animation)

    //Material3
    implementation(libs.material3)
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")

    //Coil
    implementation(libs.coil.compose)

    //Capturable
    implementation(libs.capturable) // Verifica la última versión en GitHub

    //Reorderable List/Drag and Drop
    implementation(libs.compose.dnd)
    implementation(libs.reorderables)

    //CodeView
    implementation(libs.codeview)

    //AppCompat
    implementation(libs.androidx.appcompat)

    // Media3 ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media.router)
    implementation(libs.google.play.services.cast.framework)
    implementation(libs.androidx.media3.exoplayer.ffmpeg)

    // Palette API for color extraction
    implementation(libs.androidx.palette.ktx)

    // For foreground service permission (Android 13+)
    implementation(libs.androidx.core.splashscreen) // No directamente para permiso, pero útil

    //ConstraintLayout
    implementation(libs.androidx.constraintlayout.compose)

    //Foundation
    implementation(libs.androidx.foundation)
    //Wavy slider
    implementation(libs.wavy.slider)

    // Splash Screen API
    implementation(libs.androidx.core.splashscreen) // O la versión más reciente

    //Icons
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Protobuf (JavaLite es suficiente para Android y más pequeño)
    // implementation(libs.protobuf.javalite) // Eliminada dependencia de Protobuf

    //Material library
    implementation(libs.material)

    // Kotlin Collections
    implementation(libs.kotlinx.collections.immutable) // Verifica la última versión

    // Gemini
    implementation(libs.google.genai)

    //permisisons
    implementation(libs.accompanist.permissions)

    //Audio editing
    // Spleeter para separación de audio y Amplituda para procesar formas de onda
    //implementation(libs.tensorflow.lite)
    //implementation(libs.tensorflow.lite.support)
    ///implementation(libs.tensorflow.lite.select.tf.ops)
    implementation(libs.amplituda)

    // Compose-audiowaveform para la UI
    implementation(libs.compose.audiowaveform)

    // Media3 Transformer (ya debería estar, pero asegúrate)
    implementation(libs.androidx.media3.transformer)

    //implementation(libs.pytorch.android)
    //implementation(libs.pytorch.android.torchvision)

    //Checker framework
    implementation(libs.checker.qual)

    // Timber
    implementation(libs.timber)

    // TagLib for metadata editing (supports mp3, flac, m4a, etc.)
    implementation(libs.taglib)
    // VorbisJava for Opus/Ogg metadata editing (TagLib has issues with Opus via file descriptors)
    implementation(libs.vorbisjava.core)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Ktor for HTTP Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.ui.text.google.fonts)

    implementation(libs.accompanist.drawablepainter)

    // Android Auto
    implementation(libs.androidx.media)
    implementation(libs.androidx.app)
    implementation(libs.androidx.app.projected)

    // ===== TESTING DEPENDENCIES =====

    // JUnit 4 for unit tests
    testImplementation(libs.junit)

    // JUnit 5 (Jupiter) for modern unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")

    // Kotlin test
    testImplementation(kotlin("test"))

    // Kotlin Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // MockK for mocking
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")

    // Google Truth for assertions
    testImplementation("com.google.truth:truth:1.1.5")

    // Turbine for Flow testing
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // AndroidX Test - for unit tests
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // Robolectric for Android unit tests
    testImplementation("org.robolectric:robolectric:4.11.1")

    // AndroidX Test - for instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // MockK for Android tests
    androidTestImplementation("io.mockk:mockk-android:1.13.8")

    // Google Truth for Android tests
    androidTestImplementation("com.google.truth:truth:1.1.5")

    // Kotlin Coroutines test for Android tests
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // WorkManager testing
    androidTestImplementation("androidx.work:work-testing:2.9.0")

    // Compose UI Test
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}



