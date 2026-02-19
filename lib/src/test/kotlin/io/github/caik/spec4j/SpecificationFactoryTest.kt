package io.github.caik.spec4j

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpecificationFactoryTest {
    // ==================== anyOf tests ====================

    @Test
    fun `anyOf passes when first spec passes`() {
        val composite =
            SpecificationFactory.anyOf(
                "AnyPasses",
                TestSpecs.alwaysPass,
                TestSpecs.alwaysFail,
            )

        val result = composite.evaluate(TestContext())
        assertTrue(result.passed())
        assertEquals("AnyPasses", result.name)
    }

    @Test
    fun `anyOf passes when second spec passes`() {
        val composite =
            SpecificationFactory.anyOf(
                "AnyPasses",
                TestSpecs.alwaysFail,
                TestSpecs.alwaysPass,
            )

        val result = composite.evaluate(TestContext())
        assertTrue(result.passed())
    }

    @Test
    fun `anyOf fails when all specs fail`() {
        val composite =
            SpecificationFactory.anyOf(
                "AllFail",
                TestSpecs.isAdult,
                TestSpecs.hasFunds,
            )

        val result = composite.evaluate(TestContext(age = 16, balance = 50.0))
        assertFalse(result.passed())
        assertEquals("AllFail", result.name)
        assertEquals(
            listOf(TestFailureReason.TOO_YOUNG, TestFailureReason.INSUFFICIENT_FUNDS),
            result.failureReasons,
        )
    }

    @Test
    fun `anyOf short-circuits on first pass by default`() {
        var secondEvaluated = false
        val trackingSpec =
            Specification<TestContext, TestFailureReason> {
                secondEvaluated = true
                SpecificationResult.pass("Tracking")
            }

        val composite =
            SpecificationFactory.anyOf(
                "ShortCircuit",
                TestSpecs.alwaysPass,
                trackingSpec,
            )

        composite.evaluate(TestContext())
        assertFalse(secondEvaluated, "Second spec should not be evaluated when first passes")
    }

    @Test
    fun `anyOf evaluates all when evaluateAll is true`() {
        var secondEvaluated = false
        val trackingSpec =
            Specification<TestContext, TestFailureReason> {
                secondEvaluated = true
                SpecificationResult.pass("Tracking")
            }

        val composite =
            SpecificationFactory.anyOf(
                "EvaluateAll",
                true,
                TestSpecs.alwaysPass,
                trackingSpec,
            )

        composite.evaluate(TestContext())
        assertTrue(secondEvaluated, "Second spec should be evaluated when evaluateAll is true")
    }

    @Test
    fun `anyOf throws when no specifications provided`() {
        assertFailsWith<IllegalArgumentException> {
            SpecificationFactory.anyOf<TestContext, TestFailureReason>("Empty")
        }
    }

    // ==================== allOf tests ====================

    @Test
    fun `allOf passes when all specs pass`() {
        val composite =
            SpecificationFactory.allOf(
                "AllPass",
                TestSpecs.isAdult,
                TestSpecs.hasFunds,
                TestSpecs.isVerified,
            )

        val result = composite.evaluate(TestContext(age = 25, balance = 500.0, verified = true))
        assertTrue(result.passed())
        assertEquals("AllPass", result.name)
    }

    @Test
    fun `allOf fails when one spec fails`() {
        val composite =
            SpecificationFactory.allOf(
                "OneFails",
                TestSpecs.isAdult,
                TestSpecs.hasFunds,
            )

        val result = composite.evaluate(TestContext(age = 25, balance = 50.0))
        assertFalse(result.passed())
        assertEquals(listOf(TestFailureReason.INSUFFICIENT_FUNDS), result.failureReasons)
    }

    @Test
    fun `allOf collects all failure reasons`() {
        val composite =
            SpecificationFactory.allOf(
                "MultipleFail",
                TestSpecs.isAdult,
                TestSpecs.hasFunds,
                TestSpecs.isVerified,
            )

        val result = composite.evaluate(TestContext(age = 16, balance = 50.0, verified = false))
        assertFalse(result.passed())
        assertEquals(
            listOf(
                TestFailureReason.TOO_YOUNG,
                TestFailureReason.INSUFFICIENT_FUNDS,
                TestFailureReason.NOT_VERIFIED,
            ),
            result.failureReasons,
        )
    }

    @Test
    fun `allOf throws when no specifications provided`() {
        assertFailsWith<IllegalArgumentException> {
            SpecificationFactory.allOf<TestContext, TestFailureReason>("Empty")
        }
    }

    // ==================== not tests ====================

    @Test
    fun `not inverts passing spec to failing`() {
        val composite =
            SpecificationFactory.not(
                "NotBlocked",
                TestFailureReason.BLOCKED,
                TestSpecs.alwaysPass,
            )

        val result = composite.evaluate(TestContext())
        assertFalse(result.passed())
        assertEquals("NotBlocked", result.name)
        assertEquals(listOf(TestFailureReason.BLOCKED), result.failureReasons)
    }

    @Test
    fun `not inverts failing spec to passing`() {
        val composite =
            SpecificationFactory.not(
                "NotBlocked",
                TestFailureReason.BLOCKED,
                TestSpecs.alwaysFail,
            )

        val result = composite.evaluate(TestContext())
        assertTrue(result.passed())
        assertEquals("NotBlocked", result.name)
    }

    @Test
    fun `not with real spec inverts correctly`() {
        // "Is NOT an adult" - passes when age < 18
        val isMinor =
            SpecificationFactory.not(
                "IsMinor",
                TestFailureReason.TOO_OLD,
                TestSpecs.isAdult,
            )

        assertTrue(isMinor.evaluate(TestContext(age = 16)).passed())
        assertFalse(isMinor.evaluate(TestContext(age = 25)).passed())
    }

    // ==================== nested composite tests ====================

    @Test
    fun `nested anyOf inside allOf works correctly`() {
        // Must be adult AND (have funds OR be verified)
        val composite =
            SpecificationFactory.allOf(
                "AdultAndFundsOrVerified",
                TestSpecs.isAdult,
                SpecificationFactory.anyOf("FundsOrVerified", TestSpecs.hasFunds, TestSpecs.isVerified),
            )

        // Adult with funds - passes
        assertTrue(composite.evaluate(TestContext(age = 25, balance = 500.0, verified = false)).passed())
        // Adult and verified - passes
        assertTrue(composite.evaluate(TestContext(age = 25, balance = 50.0, verified = true)).passed())
        // Minor - fails
        assertFalse(composite.evaluate(TestContext(age = 16, balance = 500.0, verified = true)).passed())
        // Adult without funds and not verified - fails
        assertFalse(composite.evaluate(TestContext(age = 25, balance = 50.0, verified = false)).passed())
    }

    @Test
    fun `nested allOf inside anyOf works correctly`() {
        // Either (adult AND has funds) OR is verified
        val composite =
            SpecificationFactory.anyOf(
                "AdultWithFundsOrVerified",
                SpecificationFactory.allOf("AdultWithFunds", TestSpecs.isAdult, TestSpecs.hasFunds),
                TestSpecs.isVerified,
            )

        // Adult with funds - passes
        assertTrue(composite.evaluate(TestContext(age = 25, balance = 500.0, verified = false)).passed())
        // Verified minor - passes
        assertTrue(composite.evaluate(TestContext(age = 16, balance = 50.0, verified = true)).passed())
        // Not verified minor without funds - fails
        assertFalse(composite.evaluate(TestContext(age = 16, balance = 50.0, verified = false)).passed())
    }

    @Test
    fun `list-based anyOf works`() {
        val specs = listOf(TestSpecs.isAdult, TestSpecs.hasFunds)
        val composite = SpecificationFactory.anyOf("FromList", specs)

        assertTrue(composite.evaluate(TestContext(age = 25, balance = 50.0)).passed())
    }

    @Test
    fun `list-based allOf works`() {
        val specs = listOf(TestSpecs.isAdult, TestSpecs.hasFunds)
        val composite = SpecificationFactory.allOf("FromList", specs)

        assertTrue(composite.evaluate(TestContext(age = 25, balance = 500.0)).passed())
        assertFalse(composite.evaluate(TestContext(age = 16, balance = 500.0)).passed())
    }
}
