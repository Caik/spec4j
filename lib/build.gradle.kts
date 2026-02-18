/*
 * Build configuration for spec4j core library.
 */

plugins {
    `java-library`
    jacoco
}

// Set the artifact name to 'spec4j' (independent of directory name)
base {
    archivesName = "spec4j"
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

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }

    // Exclude Kotlin-generated synthetic classes from coverage reports
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    // Default interface implementations
                    $$"**/*$DefaultImpls.class",
                    // When expression mappings
                    $$"**/*$WhenMappings.class",
                    // Companion object classes (if not testing them directly)
                    $$"**/*$Companion.class",
                )
            }
        })
    )
}

