/*
 * Build configuration for spec4j examples.
 *
 * Run examples with:
 *   ./gradlew :examples:run                                    # Runs default (LoanEligibility)
 *   ./gradlew :examples:run -PexampleName=LoanEligibility      # Loan eligibility example
 *   ./gradlew :examples:run -PexampleName=OrderValidation      # E-commerce order validation
 *   ./gradlew :examples:run -PexampleName=FeatureAccess        # Feature access control
 *
 * Java examples:
 *   ./gradlew :examples:run -PexampleName=UserRegistration     # User registration (Java, simple)
 *   ./gradlew :examples:run -PexampleName=InsuranceClaim       # Insurance claim processing (Java, advanced)
 */

plugins {
    application
    java
}

dependencies {
    implementation(project(":lib"))
}

// Map short example names to their full class paths
val examples =
    mapOf(
        // Kotlin examples
        "LoanEligibility" to "io.github.caik.spec4j.examples.loan.LoanEligibilityExampleKt",
        "OrderValidation" to "io.github.caik.spec4j.examples.ecommerce.OrderValidationExampleKt",
        "FeatureAccess" to "io.github.caik.spec4j.examples.accesscontrol.FeatureAccessExampleKt",
        // Java examples
        "UserRegistration" to "io.github.caik.spec4j.examples.registration.UserRegistrationExample",
        "InsuranceClaim" to "io.github.caik.spec4j.examples.insurance.InsuranceClaimExample",
    )

application {
    val exampleName = project.findProperty("exampleName") as String? ?: "LoanEligibility"
    val resolvedClass =
        examples[exampleName]
            ?: error("Unknown example: '$exampleName'. Available: ${examples.keys.joinToString()}")

    mainClass.set(resolvedClass)
}
