/*
 * Build configuration for spec4j core library.
 */

plugins {
    `java-library`
    jacoco
    alias(libs.plugins.maven.publish)
}

// Set the artifact name to 'spec4j-core' (independent of directory name)
base {
    archivesName = "spec4j-core"
}

dependencies {
}

// Maven Central publishing configuration using Vanniktech plugin
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(project.group.toString(), "spec4j-core", project.version.toString())

    pom {
        name.set("spec4j-core")
        description.set("A Kotlin implementation of the Specification Pattern for composable, reusable business rules")
        inceptionYear.set("2026")
        url.set("https://github.com/Caik/spec4j")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("caik")
                name.set("Carlos Henrique Severino")
                url.set("https://github.com/Caik")
            }
        }

        scm {
            url.set("https://github.com/Caik/spec4j")
            connection.set("scm:git:git://github.com/Caik/spec4j.git")
            developerConnection.set("scm:git:ssh://git@github.com/Caik/spec4j.git")
        }
    }
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
                    // Companion object classes
                    $$"**/*$Companion.class",
                )
            }
        })
    )
}

