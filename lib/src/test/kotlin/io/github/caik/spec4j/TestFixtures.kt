package io.github.caik.spec4j

/**
 * Shared test fixtures for spec4j tests.
 *
 * Simple failure reason enum for testing
 */
enum class TestFailureReason {
    TOO_YOUNG,
    TOO_OLD,
    INSUFFICIENT_FUNDS,
    NOT_VERIFIED,
    BLOCKED,
}

/** Simple context for testing */
data class TestContext(
    val age: Int = 25,
    val balance: Double = 1000.0,
    val verified: Boolean = true,
    val blocked: Boolean = false,
)

/** Reusable test specifications */
object TestSpecs {
    val isAdult =
        Specification.of<TestContext, TestFailureReason>(
            "IsAdult",
            { it.age >= 18 },
            TestFailureReason.TOO_YOUNG,
        )

    val hasFunds =
        Specification.of<TestContext, TestFailureReason>(
            "HasFunds",
            { it.balance >= 100 },
            TestFailureReason.INSUFFICIENT_FUNDS,
        )

    val isVerified =
        Specification.of<TestContext, TestFailureReason>(
            "IsVerified",
            { it.verified },
            TestFailureReason.NOT_VERIFIED,
        )

    val isNotBlocked =
        Specification.of<TestContext, TestFailureReason>(
            "IsNotBlocked",
            { !it.blocked },
            TestFailureReason.BLOCKED,
        )

    /** Always passes. Failure reason is never used since predicate always returns true. */
    val alwaysPass =
        Specification.of<TestContext, TestFailureReason>(
            "AlwaysPass",
            { true },
            TestFailureReason.BLOCKED,
        )

    /** Always fails */
    val alwaysFail =
        Specification.of<TestContext, TestFailureReason>(
            "AlwaysFail",
            { false },
            TestFailureReason.BLOCKED,
        )
}
