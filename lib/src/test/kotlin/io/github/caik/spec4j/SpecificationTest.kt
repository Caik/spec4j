package io.github.caik.spec4j

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpecificationTest {

    @Test
    fun `of creates specification that passes when predicate is true`() {
        val spec = Specification.of<TestContext, TestFailureReason>(
            "AgeCheck",
            { it.age >= 18 },
            TestFailureReason.TOO_YOUNG
        )

        val result = spec.evaluate(TestContext(age = 25))

        assertTrue(result.passed())
        assertEquals("AgeCheck", result.name)
    }

    @Test
    fun `of creates specification that fails when predicate is false`() {
        val spec = Specification.of<TestContext, TestFailureReason>(
            "AgeCheck",
            { it.age >= 18 },
            TestFailureReason.TOO_YOUNG
        )

        val result = spec.evaluate(TestContext(age = 16))

        assertFalse(result.passed())
        assertEquals("AgeCheck", result.name)
        assertEquals(listOf(TestFailureReason.TOO_YOUNG), result.failureReasons)
    }

    @Test
    fun `custom specification implementation works`() {
        class CustomSpec : Specification<TestContext, TestFailureReason> {
            override fun evaluate(context: TestContext): SpecificationResult<TestFailureReason> {
                return if (context.age >= 21 && context.verified) {
                    SpecificationResult.pass(name())
                } else {
                    SpecificationResult.fail(name(), TestFailureReason.NOT_VERIFIED)
                }
            }
        }

        val spec = CustomSpec()

        val passResult = spec.evaluate(TestContext(age = 25, verified = true))
        assertTrue(passResult.passed())
        assertEquals("CustomSpec", passResult.name)

        val failResult = spec.evaluate(TestContext(age = 25, verified = false))
        assertFalse(failResult.passed())
    }

    @Test
    fun `name defaults to class simple name for custom implementations`() {
        class MySpecification : Specification<TestContext, TestFailureReason> {
            override fun evaluate(context: TestContext): SpecificationResult<TestFailureReason> =
                SpecificationResult.pass(name())
        }

        val spec = MySpecification()
        assertEquals("MySpecification", spec.name())
    }

    @Test
    fun `lambda specification has generated name`() {
        val spec = Specification<TestContext, TestFailureReason> { _ ->
            SpecificationResult.pass("LambdaSpec")
        }

        // Lambda names are implementation-specific, but shouldn't throw
        val name = spec.name()
        assertTrue(name.isNotEmpty())
    }

    @Test
    fun `specification can use complex predicate logic`() {
        val spec = Specification.of<TestContext, TestFailureReason>(
            "ComplexCheck",
            { it.age >= 18 && it.balance > 500 && it.verified && !it.blocked },
            TestFailureReason.BLOCKED
        )

        assertTrue(spec.evaluate(TestContext(age = 25, balance = 1000.0, verified = true, blocked = false)).passed())
        assertFalse(spec.evaluate(TestContext(age = 17, balance = 1000.0, verified = true, blocked = false)).passed())
        assertFalse(spec.evaluate(TestContext(age = 25, balance = 100.0, verified = true, blocked = false)).passed())
        assertFalse(spec.evaluate(TestContext(age = 25, balance = 1000.0, verified = false, blocked = false)).passed())
        assertFalse(spec.evaluate(TestContext(age = 25, balance = 1000.0, verified = true, blocked = true)).passed())
    }
}

