package io.github.caik.spec4j.examples.loan

import io.github.caik.spec4j.Policy
import io.github.caik.spec4j.Specification
import io.github.caik.spec4j.SpecificationFactory

// Example failure reason enum
enum class LoanIneligibilityReason {
    AGE_TOO_YOUNG,
    AGE_TOO_OLD,
    INSUFFICIENT_INCOME,
    POOR_CREDIT_SCORE,
    NOT_EMPLOYED,
}

// Example context data class
data class LoanApplicationContext(
    val age: Int,
    val annualIncome: Double,
    val creditScore: Int,
    val employed: Boolean,
)

/**
 * Example demonstrating how to use the Specification/Policy framework.
 */
fun main() {
    // Define individual specifications using Specification.of(name, predicate, failureReason)
    val ageMinimum =
        Specification.of<LoanApplicationContext, LoanIneligibilityReason>(
            "AgeMinimum",
            { it.age >= 18 },
            LoanIneligibilityReason.AGE_TOO_YOUNG,
        )

    val ageMaximum =
        Specification.of<LoanApplicationContext, LoanIneligibilityReason>(
            "AgeMaximum",
            { it.age <= 65 },
            LoanIneligibilityReason.AGE_TOO_OLD,
        )

    val minimumIncome =
        Specification.of<LoanApplicationContext, LoanIneligibilityReason>(
            "MinimumIncome",
            { it.annualIncome >= 30000 },
            LoanIneligibilityReason.INSUFFICIENT_INCOME,
        )

    val goodCreditScore =
        Specification.of<LoanApplicationContext, LoanIneligibilityReason>(
            "GoodCreditScore",
            { it.creditScore >= 650 },
            LoanIneligibilityReason.POOR_CREDIT_SCORE,
        )

    val isEmployed =
        Specification.of<LoanApplicationContext, LoanIneligibilityReason>(
            "IsEmployed",
            { it.employed },
            LoanIneligibilityReason.NOT_EMPLOYED,
        )

    // Composite specification: either good credit OR (employed AND decent income)
    val financiallyQualified =
        SpecificationFactory.anyOf(
            "FinanciallyQualified",
            goodCreditScore,
            SpecificationFactory.allOf("EmployedWithIncome", isEmployed, minimumIncome),
        )

    // Build the policy (a named collection of specifications)
    val loanEligibilityPolicy =
        Policy.create<LoanApplicationContext, LoanIneligibilityReason>()
            .with(ageMinimum)
            .with(ageMaximum)
            .with(financiallyQualified)

    // Test cases
    println("=== Specification/Policy Framework Example ===\n")

    // Eligible applicant (good credit)
    val applicant1 = LoanApplicationContext(30, 50000.0, 720, true)
    runTest("Good credit, employed", applicant1, loanEligibilityPolicy)

    // Eligible applicant (employed with income, but poor credit)
    val applicant2 = LoanApplicationContext(25, 45000.0, 600, true)
    runTest("Poor credit but employed with good income", applicant2, loanEligibilityPolicy)

    // Too young
    val applicant3 = LoanApplicationContext(17, 50000.0, 720, true)
    runTest("Too young (17)", applicant3, loanEligibilityPolicy)

    // Too old
    val applicant4 = LoanApplicationContext(70, 80000.0, 750, true)
    runTest("Too old (70)", applicant4, loanEligibilityPolicy)

    // Poor credit AND not employed
    val applicant5 = LoanApplicationContext(35, 20000.0, 550, false)
    runTest("Poor credit and not employed", applicant5, loanEligibilityPolicy)

    // Using evaluateAll to collect all failures
    val applicant6 = LoanApplicationContext(16, 10000.0, 500, false)
    val allFailures = loanEligibilityPolicy.evaluateAll(applicant6)

    println("\n--- Using evaluateAll (collects all failures) ---")
    println("Multiple issues (age 16, low income, poor credit, unemployed)")
    println("  Context: $applicant6")
    println("  Passed: ${allFailures.allPassed}")
    println("  All failure reasons: ${allFailures.failureReasons()}")
    println("  Failed specifications:")

    for (failed in allFailures.failedResults()) {
        println("    - ${failed.name}: ${failed.failureReasons}")
    }
}

private fun <T, R : Enum<R>> runTest(
    testName: String,
    context: T,
    policy: Policy<T, R>,
) {
    val result = policy.evaluateFailFast(context)

    println(testName)
    println("  Context: $context")

    if (result.allPassed) {
        println("  Result: ✅ ELIGIBLE")
    } else {
        println("  Result: ❌ INELIGIBLE - ${result.failureReasons()}")
    }

    println()
}
