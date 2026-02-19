package io.github.caik.spec4j

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyResultTest {
    @Test
    fun `failedResults returns only failed specifications`() {
        val results: List<SpecificationResult<TestFailureReason>> =
            listOf(
                SpecificationResult.pass("Spec1"),
                SpecificationResult.fail("Spec2", TestFailureReason.TOO_YOUNG),
                SpecificationResult.pass("Spec3"),
                SpecificationResult.fail("Spec4", TestFailureReason.BLOCKED),
            )

        val policyResult = PolicyResult(allPassed = false, results = results)
        val failed = policyResult.failedResults()

        assertEquals(2, failed.size)
        assertEquals("Spec2", failed[0].name)
        assertEquals("Spec4", failed[1].name)
    }

    @Test
    fun `failedResults returns empty list when all pass`() {
        val results: List<SpecificationResult<TestFailureReason>> =
            listOf(
                SpecificationResult.pass("Spec1"),
                SpecificationResult.pass("Spec2"),
            )

        val policyResult = PolicyResult(allPassed = true, results = results)

        assertTrue(policyResult.failedResults().isEmpty())
    }

    @Test
    fun `failureReasons returns flattened list of all failure reasons`() {
        val results: List<SpecificationResult<TestFailureReason>> =
            listOf(
                SpecificationResult.pass("Spec1"),
                SpecificationResult.fail("Spec2", TestFailureReason.TOO_YOUNG, TestFailureReason.INSUFFICIENT_FUNDS),
                SpecificationResult.fail("Spec3", TestFailureReason.BLOCKED),
            )

        val policyResult = PolicyResult(allPassed = false, results = results)

        assertEquals(
            listOf(
                TestFailureReason.TOO_YOUNG,
                TestFailureReason.INSUFFICIENT_FUNDS,
                TestFailureReason.BLOCKED,
            ),
            policyResult.failureReasons(),
        )
    }

    @Test
    fun `failureReasons returns empty list when all pass`() {
        val results: List<SpecificationResult<TestFailureReason>> =
            listOf(
                SpecificationResult.pass("Spec1"),
                SpecificationResult.pass("Spec2"),
            )

        val policyResult = PolicyResult(allPassed = true, results = results)

        assertTrue(policyResult.failureReasons().isEmpty())
    }

    @Test
    fun `allPassed reflects overall success`() {
        val successResult = PolicyResult<TestFailureReason>(allPassed = true, results = emptyList())
        val failureResult = PolicyResult<TestFailureReason>(allPassed = false, results = emptyList())

        assertTrue(successResult.allPassed)
        assertFalse(failureResult.allPassed)
    }

    @Test
    fun `data class equality works correctly`() {
        val results1 = listOf(SpecificationResult.pass<TestFailureReason>("Spec1"))
        val results2 = listOf(SpecificationResult.pass<TestFailureReason>("Spec1"))

        val policyResult1 = PolicyResult(allPassed = true, results = results1)
        val policyResult2 = PolicyResult(allPassed = true, results = results2)

        assertEquals(policyResult1, policyResult2)
    }
}
