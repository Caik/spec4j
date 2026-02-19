/*
 * Build configuration for spec4j examples.
 *
 * Run examples with:
 *   ./gradlew :examples:run                                    # Runs default (LoanEligibility)
 *   ./gradlew :examples:run -PexampleName=LoanEligibility      # Loan eligibility example
 *   ./gradlew :examples:run -PexampleName=OrderValidation      # E-commerce order validation
 *   ./gradlew :examples:run -PexampleName=FeatureAccess        # Feature access control
 */

plugins {
    application
}

dependencies {
    implementation(project(":lib"))
}

// Map short example names to their full class paths
val examples =
    mapOf(
        "LoanEligibility" to "io.github.caik.spec4j.examples.loan.LoanEligibilityExampleKt",
        "OrderValidation" to "io.github.caik.spec4j.examples.ecommerce.OrderValidationExampleKt",
        "FeatureAccess" to "io.github.caik.spec4j.examples.accesscontrol.FeatureAccessExampleKt",
    )

application {
    val exampleName = project.findProperty("exampleName") as String? ?: "LoanEligibility"
    val resolvedClass =
        examples[exampleName]
            ?: error("Unknown example: '$exampleName'. Available: ${examples.keys.joinToString()}")

    mainClass.set(resolvedClass)
}
