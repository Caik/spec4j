/*
 * Root build configuration for spec4j multi-module project.
 */

plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
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

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
