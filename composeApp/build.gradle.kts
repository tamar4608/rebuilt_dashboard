import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("org.photonvision.tools.WpilibTools")
}

val jniPlatform: String by rootProject.extra
val wpilibVersion: String by rootProject.extra
val openCVYear: String by rootProject.extra
val openCVversion: String by rootProject.extra

wpilibTools.deps.setWpilibVersion(wpilibVersion)

val nativeConfigName = "wpilibNatives"
val wpilibNatives by configurations.creating
val nativeTasks = wpilibTools.createExtractionTasks {
    configurationName = nativeConfigName
}

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
            implementation(libs.wpilibj)
            implementation(libs.ntcore)
            implementation(libs.wpiutil)
            implementation(libs.jackson.core)
            api("edu.wpi.first.ntcore:ntcore-java:$wpilibVersion")
            api("edu.wpi.first.ntcore:ntcore-jni:$wpilibVersion")
            api("edu.wpi.first.ntcore:ntcore-jni:$wpilibVersion:$jniPlatform")
        }
    }
}

dependencies {
    add(nativeConfigName, wpilibTools.deps.wpilib("wpimath"))
    add(nativeConfigName, wpilibTools.deps.wpilib("wpinet"))
    add(nativeConfigName, wpilibTools.deps.wpilib("wpiutil"))
    add(nativeConfigName, wpilibTools.deps.wpilib("ntcore"))
}

val wpilibNativesDir = nativeTasks.assemble.map { it.destinationDir }
val wpilibNativesLibDir = wpilibNativesDir.map { it.resolve("linux/x86-64/shared") }
tasks.withType<JavaExec>().configureEach {
    dependsOn(nativeTasks.assemble)
    systemProperty("java.library.path", wpilibNativesLibDir.get().absolutePath)
    jvmArgs("-Djava.library.path=${wpilibNativesLibDir.get().absolutePath}")
}

tasks.withType<org.photonvision.tools.ExtractConfiguration>().configureEach {
    notCompatibleWithConfigurationCache("WpilibTools extract tasks use configuration cache-unsupported types.")
}

tasks.withType<org.photonvision.tools.FixupNativeResources>().configureEach {
    notCompatibleWithConfigurationCache("WpilibTools fixup tasks use Project at execution time.")
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
