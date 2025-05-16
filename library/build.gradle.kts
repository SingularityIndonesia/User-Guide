import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.singularityuniverse.lib"
version = "1.0.0"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // linuxX64() // Removed Linux target to fix compatibility issues
    
    // Apply the default hierarchy template to auto-configure source sets
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Kotlin coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                // Android-specific compose dependencies
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.ui.tooling)  // Juga perlu UI tooling untuk preview
                implementation(libs.compose.material3)
                implementation(libs.compose.material3.window.size)
                implementation(libs.accompanist.systemuicontroller)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.compose.ui.tooling)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
        // iOS source sets are automatically configured by the hierarchy template
    }
}

android {
    namespace = "com.singularityuniverse.lib.userguide"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//
//    signAllPublications()
//
//    coordinates(group.toString(), "userguide", version.toString())
//
//    pom {
//        name = "User Guide"
//        description = "Jetpack Compose library to interactively guide the user."
//        inceptionYear = "2025"
//        url = "https://github.com/SingularityIndonesia/User-Guide"
//        licenses {
//            license {
//                name = "The Apache License, Version 2.0"
//                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
//                distribution = "repo"
//            }
//        }
//        developers {
//            developer {
//                id = "singularity"
//                name = "Singularity Indonesia"
//                url = "https://github.com/SingularityIndonesia"
//            }
//        }
//        scm {
//            url = "https://github.com/SingularityIndonesia/User-Guide"
//            connection = "scm:git:https://github.com/SingularityIndonesia/User-Guide.git"
//            developerConnection = "scm:git:ssh://git@github.com:SingularityIndonesia/User-Guide.git"
//        }
//    }
//}