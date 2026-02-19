/*
 * Root build configuration for spec4j multi-module project.
 */

plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    group = "io.github.caik"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    // Configure ktlint
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude("*.gradle.kts")
        }
    }

    // Auto-format code before compiling
    tasks.named("compileKotlin") {
        dependsOn("ktlintFormat")
    }

    tasks.named("compileTestKotlin") {
        dependsOn("ktlintFormat")
    }

    // Disable ktlintCheck tasks (we auto-format instead)
    tasks.matching { it.name.startsWith("ktlint") && it.name.contains("Check") }.configureEach {
        enabled = false
    }
}
