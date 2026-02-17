/*
 * Build configuration for spec4j examples.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "io.github.caik"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("io.github.caik.spec4j.examples.LoanEligibilityExampleKt")
}

