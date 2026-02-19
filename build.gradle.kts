import org.jlleitschuh.gradle.ktlint.KtlintExtension

/*
 * Root build configuration for spec4j multi-module project.
 */

plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.axion.release)
}

// Configure axion-release for semantic versioning
scmVersion {
    tag {
        prefix.set("v")
    }

    // Push tags only (not branch) on release
    repository {
        pushTagsOnly.set(true)
    }
}

// Store version from root project for subprojects
val projectVersion = scmVersion.version

allprojects {
    group = "io.github.caik"
    version = projectVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    // Configure ktlint
    extensions.configure<KtlintExtension> {
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

    // Skip ktlintCheck when auto-formatting
    // CI runs without this flag to catch unformatted code
    // Usage: ./gradlew build -PskipFormatCheck
    tasks.matching { it.name.startsWith("ktlint") && it.name.contains("Check") }.configureEach {
        enabled = !project.hasProperty("skipFormatCheck")
    }
}
