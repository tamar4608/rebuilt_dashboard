import edu.wpi.first.toolchain.*
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("cpp")
    id("edu.wpi.first.wpilib.repositories.WPILibRepositoriesPlugin") version "2020.2"
    id("edu.wpi.first.GradleRIO") version "2026.2.1"
    id("org.photonvision.tools.WpilibTools") version "2.4.1-photon"
    id("edu.wpi.first.GradleJni") version "1.1.0"
    id("com.gradleup.shadow") version "8.3.4" apply false
    id("com.github.node-gradle.node") version "7.0.1" apply false
    // avoid loading Compose plugins multiple times in subprojects
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

allprojects {
    apply(plugin = "edu.wpi.first.wpilib.repositories.WPILibRepositoriesPlugin")
    repositories {
        google()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://frcmaven.wpi.edu/artifactory/ex-mvn/")
        mavenCentral()
        mavenLocal()
        maven(url = "https://maven.photonvision.org/releases")
        maven(url = "https://maven.photonvision.org/snapshots")
    }
    wpilibRepositories.addAllReleaseRepositories(this)
    wpilibRepositories.addAllDevelopmentRepositories(this)
}

extra["localMavenURL"] = file("${project.buildDir}/outputs/maven")
extra["allOutputsFolder"] = file("${project.buildDir}/outputs")

// Configure the version number.
apply(from = "versioningHelper.gradle")

val versionString: String by extra

val wpilibVersion = "2026.2.1"
val pubVersion = versionString
val isDev = pubVersion.startsWith("dev")
val wpilibNativeName = wpilibTools.platformMapper.currentPlatform.platformName
val jniPlatform = wpilibTools.platformMapper.wpilibClassifier

extra.apply {
    set("wpilibVersion", wpilibVersion)
    set("wpimathVersion", wpilibVersion)
    set("javalinVersion", "6.7.0")
    set("frcYear", "2026")
    set("pubVersion", pubVersion)
    set("isDev", isDev)
    set("wpilibNativeName", wpilibNativeName)
    set("jniPlatform", jniPlatform)
}

println("Using Wpilib: $wpilibVersion")



tasks.wrapper {
    gradleVersion = "8.14.3"
}

extra["getCurrentArch"] = {
    NativePlatforms.desktop
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-XDstringConcat=inline")
        options.encoding = "UTF-8"
    }

    // Enables UTF-8 support in Javadoc
    tasks.withType<Javadoc>().configureEach {
        val docletOptions = options as StandardJavadocDocletOptions
        docletOptions.addStringOption("charset", "utf-8")
        docletOptions.addStringOption("docencoding", "utf-8")
        docletOptions.addStringOption("encoding", "utf-8")
        docletOptions.addBooleanOption(
            "Xdoclint/package:-org.photonvision.proto,-org.photonvision.struct,-org.photonvision.targeting.proto,-org.photonvision.jni",
            true,
        )
    }
}
