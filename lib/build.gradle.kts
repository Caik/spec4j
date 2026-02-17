/*
 * Build configuration for spec4j core library.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "io.github.caik"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // No external dependencies required for the core library
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("2.2.20")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

