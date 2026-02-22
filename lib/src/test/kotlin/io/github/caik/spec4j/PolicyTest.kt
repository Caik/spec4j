package io.github.caik.spec4j

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PolicyTest {
    // ==================== DSL builder tests ====================

    @Test
    fun `policy DSL creates policy with specifications`() {
        val dslPolicy =
            policy<TestContext, TestFailureReason> {
                +TestSpecs.isAdult
                +TestSpecs.hasFunds
            }

        val result = dslPolicy.evaluateFailFast(TestContext(age = 25, balance = 500.0))
        assertTrue(result.allPassed)
    }

    @Test
    fun `policy DSL fails when spec fails`() {
        val dslPolicy =
            policy<TestContext, TestFailureReason> {
                +TestSpecs.isAdult
                +TestSpecs.hasFunds
            }

        val result = dslPolicy.evaluateFailFast(TestContext(age = 16, balance = 500.0))
        assertFalse(result.allPassed)
        assertEquals(listOf(TestFailureReason.TOO_YOUNG), result.failureReasons())
    }

    @Test
    fun `policy DSL with multiple specs evaluates all`() {
        val dslPolicy =
            policy<TestContext, TestFailureReason> {
                +TestSpecs.isAdult
                +TestSpecs.hasFunds
                +TestSpecs.isVerified
            }

        val result = dslPolicy.evaluateAll(TestContext(age = 16, balance = 50.0, verified = false))
        assertFalse(result.allPassed)
        assertEquals(3, result.failedResults().size)
    }

    @Test
    fun `policy DSL with empty block creates empty policy`() {
        val emptyPolicy = policy<TestContext, TestFailureReason> { }

        assertTrue(emptyPolicy.evaluateFailFast(TestContext()).allPassed)
    }

    @Test
    fun `policy DSL is equivalent to fluent builder`() {
        val fluentPolicy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)

        val dslPolicy =
            policy<TestContext, TestFailureReason> {
                +TestSpecs.isAdult
                +TestSpecs.hasFunds
            }

        val context = TestContext(age = 25, balance = 500.0)
        assertEquals(
            fluentPolicy.evaluateFailFast(context).allPassed,
            dslPolicy.evaluateFailFast(context).allPassed,
        )
    }

    // ==================== toString tests ====================

    @Test
    fun `toString returns expression for simple policy`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)

        val str = policy.toString()
        assertEquals("(IsAdult AND HasFunds)", str)
    }

    @Test
    fun `toString returns expression for policy with composite specs`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(SpecificationFactory.anyOf("FundsOrVerified", TestSpecs.hasFunds, TestSpecs.isVerified))

        val str = policy.toString()
        assertEquals("(IsAdult AND (HasFunds OR IsVerified))", str)
    }

    @Test
    fun `toString returns expression for nested composites`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(
                    SpecificationFactory.anyOf(
                        "FundsOrVerifiedAndNotBlocked",
                        TestSpecs.hasFunds,
                        SpecificationFactory.allOf("VerifiedAndNotBlocked", TestSpecs.isVerified, TestSpecs.isNotBlocked),
                    ),
                )

        val str = policy.toString()
        assertEquals("(IsAdult AND (HasFunds OR (IsVerified AND IsNotBlocked)))", str)
    }

    @Test
    fun `toString returns expression with NOT`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(SpecificationFactory.not("NotBlocked", TestFailureReason.BLOCKED, TestSpecs.isNotBlocked))

        val str = policy.toString()
        assertEquals("(IsAdult AND NOT IsNotBlocked)", str)
    }

    @Test
    fun `toString returns empty parentheses for empty policy`() {
        val emptyPolicy = Policy.create<TestContext, TestFailureReason>()

        assertEquals("()", emptyPolicy.toString())
    }

    @Test
    fun `toString works with DSL-created policy`() {
        val dslPolicy =
            policy<TestContext, TestFailureReason> {
                +TestSpecs.isAdult
                +TestSpecs.hasFunds
                +TestSpecs.isVerified
            }

        val str = dslPolicy.toString()
        assertEquals("(IsAdult AND HasFunds AND IsVerified)", str)
    }
    // ==================== evaluateFailFast tests ====================

    @Test
    fun `evaluateFailFast returns success when all specs pass`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)
                .with(TestSpecs.isVerified)

        val result = policy.evaluateFailFast(TestContext(age = 25, balance = 500.0, verified = true))

        assertTrue(result.allPassed)
        assertEquals(3, result.results.size)
        assertTrue(result.results.all { it.passed() })
    }

    @Test
    fun `evaluateFailFast stops on first failure`() {
        var thirdSpecEvaluated = false
        val trackingSpec =
            Specification<TestContext, TestFailureReason> {
                thirdSpecEvaluated = true
                SpecificationResult.pass("Tracking")
            }

        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult) // will fail
                .with(TestSpecs.hasFunds) // should not be evaluated
                .with(trackingSpec) // should not be evaluated

        val result = policy.evaluateFailFast(TestContext(age = 16))

        assertFalse(result.allPassed)
        assertEquals(1, result.results.size)
        assertEquals("IsAdult", result.results[0].name)
        assertFalse(thirdSpecEvaluated)
    }

    @Test
    fun `evaluateFailFast returns failure reasons from first failed spec`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)

        val result = policy.evaluateFailFast(TestContext(age = 16, balance = 500.0))

        assertFalse(result.allPassed)
        assertEquals(listOf(TestFailureReason.TOO_YOUNG), result.failureReasons())
    }

    // ==================== evaluateAll tests ====================

    @Test
    fun `evaluateAll returns success when all specs pass`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)

        val result = policy.evaluateAll(TestContext(age = 25, balance = 500.0))

        assertTrue(result.allPassed)
        assertEquals(2, result.results.size)
    }

    @Test
    fun `evaluateAll evaluates all specs even when some fail`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)
                .with(TestSpecs.isVerified)

        val result = policy.evaluateAll(TestContext(age = 16, balance = 50.0, verified = false))

        assertFalse(result.allPassed)
        assertEquals(3, result.results.size)
        assertEquals(3, result.failedResults().size)
    }

    @Test
    fun `evaluateAll collects all failure reasons`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(TestSpecs.hasFunds)
                .with(TestSpecs.isNotBlocked)

        val result = policy.evaluateAll(TestContext(age = 16, balance = 50.0, blocked = true))

        assertEquals(
            listOf(
                TestFailureReason.TOO_YOUNG,
                TestFailureReason.INSUFFICIENT_FUNDS,
                TestFailureReason.BLOCKED,
            ),
            result.failureReasons(),
        )
    }

    // ==================== Policy builder tests ====================

    @Test
    fun `policy with no specs passes`() {
        val policy = Policy.create<TestContext, TestFailureReason>()

        val failFastResult = policy.evaluateFailFast(TestContext())
        val evaluateAllResult = policy.evaluateAll(TestContext())

        assertTrue(failFastResult.allPassed)
        assertTrue(evaluateAllResult.allPassed)
    }

    @Test
    fun `with returns same policy for chaining`() {
        val policy = Policy.create<TestContext, TestFailureReason>()
        val returned = policy.with(TestSpecs.isAdult)

        assertSame(policy, returned)
    }

    @Test
    fun `policy with composite specifications`() {
        val policy =
            Policy.create<TestContext, TestFailureReason>()
                .with(TestSpecs.isAdult)
                .with(SpecificationFactory.anyOf("FundsOrVerified", TestSpecs.hasFunds, TestSpecs.isVerified))

        // Adult with funds - passes
        assertTrue(policy.evaluateFailFast(TestContext(age = 25, balance = 500.0, verified = false)).allPassed)
        // Adult and verified but no funds - passes
        assertTrue(policy.evaluateFailFast(TestContext(age = 25, balance = 50.0, verified = true)).allPassed)
        // Minor - fails
        assertFalse(policy.evaluateFailFast(TestContext(age = 16, balance = 500.0, verified = true)).allPassed)
    }
}
