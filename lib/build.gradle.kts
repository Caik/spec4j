/*
 * Build configuration for spec4j core library.
 */

plugins {
    `java-library`
    jacoco
    `maven-publish`
    signing
}

// Set the artifact name to 'spec4j-core' (independent of directory name)
base {
    archivesName = "spec4j-core"
}

dependencies {
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "spec4j-core"
            from(components["java"])

            pom {
                name = "spec4j-core"
                description = "A Kotlin implementation of the Specification Pattern for composable, reusable business rules"
                url = "https://github.com/Caik/spec4j"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                developers {
                    developer {
                        id = "caik"
                        name = "Carlos Henrique Severino"
                        url = "https://github.com/Caik"
                    }
                }

                scm {
                    url = "https://github.com/Caik/spec4j"
                    connection = "scm:git:git://github.com/Caik/spec4j.git"
                    developerConnection = "scm:git:ssh://git@github.com/Caik/spec4j.git"
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload")

            credentials {
                username = System.getenv("SONATYPE_USERNAME") ?: ""
                password = System.getenv("SONATYPE_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    } else {
        useGpgCmd()
    }

    sign(publishing.publications["mavenJava"])
}

// Only sign when publishing
tasks.withType<Sign>().configureEach {
    onlyIf { gradle.taskGraph.hasTask("publish") }
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

