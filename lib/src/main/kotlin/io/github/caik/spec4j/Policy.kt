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

    companion object {
        /**
         * Creates a new Policy builder.
         */
        fun <T, R : Enum<R>> create(): Policy<T, R> = Policy()
    }
}
