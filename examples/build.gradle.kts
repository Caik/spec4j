/*
 * Build configuration for spec4j examples.
 */

plugins {
    application
}

dependencies {
    implementation(project(":lib"))
}

application {
    mainClass.set("io.github.caik.spec4j.examples.LoanEligibilityExampleKt")
}

