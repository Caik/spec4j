package io.github.caik.spec4j

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpecificationResultTest {

    @Test
    fun `pass creates result with no failure reasons`() {
        val result = SpecificationResult.pass<TestFailureReason>("TestSpec")

        assertEquals("TestSpec", result.name)
        assertNull(result.failureReasons)
        assertTrue(result.passed())
    }

    @Test
    fun `fail with single reason creates result with one failure reason`() {
        val result = SpecificationResult.fail("TestSpec", TestFailureReason.TOO_YOUNG)

        assertEquals("TestSpec", result.name)
        assertEquals(listOf(TestFailureReason.TOO_YOUNG), result.failureReasons)
        assertFalse(result.passed())
    }

    @Test
    fun `fail with multiple reasons creates result with all failure reasons`() {
        val result = SpecificationResult.fail(
            "TestSpec",
            TestFailureReason.TOO_YOUNG,
            TestFailureReason.INSUFFICIENT_FUNDS
        )

        assertEquals("TestSpec", result.name)
        assertEquals(
            listOf(TestFailureReason.TOO_YOUNG, TestFailureReason.INSUFFICIENT_FUNDS),
            result.failureReasons
        )
        assertFalse(result.passed())
    }

    @Test
    fun `fail with list creates result with all failure reasons`() {
        val reasons = listOf(TestFailureReason.NOT_VERIFIED, TestFailureReason.BLOCKED)
        val result = SpecificationResult.fail("TestSpec", reasons)

        assertEquals("TestSpec", result.name)
        assertEquals(reasons, result.failureReasons)
        assertFalse(result.passed())
    }

    @Test
    fun `passed returns true when failureReasons is null`() {
        val result = SpecificationResult<TestFailureReason>("TestSpec", null)
        assertTrue(result.passed())
    }

    @Test
    fun `passed returns true when failureReasons is empty list`() {
        val result = SpecificationResult<TestFailureReason>("TestSpec", emptyList())
        assertTrue(result.passed())
    }

    @Test
    fun `passed returns false when failureReasons has items`() {
        val result = SpecificationResult("TestSpec", listOf(TestFailureReason.BLOCKED))
        assertFalse(result.passed())
    }

    @Test
    fun `data class equality works correctly`() {
        val result1 = SpecificationResult.fail("TestSpec", TestFailureReason.TOO_YOUNG)
        val result2 = SpecificationResult.fail("TestSpec", TestFailureReason.TOO_YOUNG)

        assertEquals(result1, result2)
    }

    @Test
    fun `constructor with default parameter creates passing result`() {
        // Directly use the data class constructor with default parameter
        val result = SpecificationResult<TestFailureReason>("TestSpec")

        assertEquals("TestSpec", result.name)
        assertNull(result.failureReasons)
        assertTrue(result.passed())
    }
}

