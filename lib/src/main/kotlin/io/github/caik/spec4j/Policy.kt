package io.github.caik.spec4j

/**
 * A policy is a named collection of specifications that together define a business rule.
 *
 * @param T the type of the context to evaluate
 * @param R the type of the failure reason (must be an enum)
 */
class Policy<T, R : Enum<R>> private constructor() {
    private val specifications = mutableListOf<Specification<T, R>>()

    /**
     * Returns a SQL-like human-readable expression representing this policy's structure.
     * Shows all specifications joined with AND.
     *
     * Example: "AgeMinimum AND AgeMaximum AND (GoodCredit OR (IsEmployed AND HasIncome))"
     */
    override fun toString(): String =
        if (specifications.isEmpty()) {
            "()"
        } else {
            "(${specifications.joinToString(" AND ") { it.toExpression() }})"
        }

    /**
     * Adds a specification to this policy.
     */
    fun with(specification: Specification<T, R>): Policy<T, R> {
        specifications.add(specification)
        return this
    }

    /**
     * Evaluates all specifications and returns on the first failure.
     *
     * @param context the context to evaluate
     * @return the policy result
     */
    fun evaluateFailFast(context: T): PolicyResult<R> {
        val results = mutableListOf<SpecificationResult<R>>()

        for (spec in specifications) {
            val result = spec.evaluate(context)
            results.add(result)

            if (!result.passed()) {
                return PolicyResult.failure(results)
            }
        }

        return PolicyResult.success(results)
    }

    /**
     * Evaluates all specifications and collects all results.
     *
     * @param context the context to evaluate
     * @return the policy result with all specification results
     */
    fun evaluateAll(context: T): PolicyResult<R> {
        val results = mutableListOf<SpecificationResult<R>>()
        var allPassed = true

        for (spec in specifications) {
            val result = spec.evaluate(context)
            results.add(result)

            if (!result.passed()) {
                allPassed = false
            }
        }

        return if (allPassed) {
            PolicyResult.success(results)
        } else {
            PolicyResult.failure(results)
        }
    }

    /**
     * DSL builder scope for adding specifications using the unary plus operator.
     */
    @PolicyDsl
    inner class Builder {
        /**
         * Adds a specification to this policy using the unary plus operator.
         * Allows `+spec` syntax within the DSL block.
         */
        operator fun Specification<T, R>.unaryPlus() {
            this@Policy.with(this)
        }
    }

    companion object {
        /**
         * Creates a new Policy builder.
         */
        @JvmStatic
        fun <T, R : Enum<R>> create(): Policy<T, R> = Policy()
    }
}

/**
 * DSL marker to prevent scope leakage in nested builders.
 */
@DslMarker
annotation class PolicyDsl

/**
 * Creates a Policy using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val policy = policy<MyContext, MyReason> {
 *     +ageCheck
 *     +incomeCheck
 *     +creditScoreCheck
 * }
 * ```
 *
 * @param T the type of the context to evaluate
 * @param R the type of the failure reason (must be an enum)
 * @param block the builder block where specifications are added using `+spec` syntax
 * @return the configured Policy
 */
inline fun <T, R : Enum<R>> policy(block: Policy<T, R>.Builder.() -> Unit): Policy<T, R> {
    val policy = Policy.create<T, R>()
    policy.Builder().apply(block)
    return policy
}
