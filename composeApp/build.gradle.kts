import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.internal.os.OperatingSystem


    val frcYear = "2026" // Ensure frcYear is defined or fetched from the wpi extension
    var frcHome: File

    if (OperatingSystem.current().isWindows) {
        val publicFolder = System.getenv("PUBLIC") ?: "C:\\Users\\Public"
        val homeRoot = File(publicFolder, "wpilib")
        frcHome = File(homeRoot, frcYear)
    } else {
        val userFolder = System.getProperty("user.home")
        val homeRoot = File(userFolder, "wpilib")
        frcHome = File(homeRoot, frcYear)
    }

    val frcHomeMaven = File(frcHome, "maven")

    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            name = "frcHome"
            url = frcHomeMaven.toURI() // Kotlin DSL prefers URIs for repository URLs
        }
    }


plugins {
    id("edu.wpi.first.GradleRIO") version "2026.2.1"
    id("edu.wpi.first.WpilibTools") version "2.1.0"
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)

}
wpilibTools.deps.wpilibVersion = wpi.versions.wpilibVersion.get()

val nativeConfigName = "wpilibNatives"
val nativeConfig = configurations.create(nativeConfigName)

val nativeTasks = wpilibTools.createExtractionTasks {
    configurationName.set(nativeConfigName)
}

nativeTasks.addToSourceSetResources(sourceSets.main.get())

nativeConfig.dependencies.add(wpilibTools.deps.wpilib("wpimath"))
nativeConfig.dependencies.add(wpilibTools.deps.wpilib("wpinet"))
nativeConfig.dependencies.add(wpilibTools.deps.wpilib("wpiutil"))
nativeConfig.dependencies.add(wpilibTools.deps.wpilib("ntcore"))


kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.example.rebuilt_dashboard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.rebuilt_dashboard"
            packageVersion = "1.0.0"
        }
    }
}
