package io.github.caik.spec4j

/**
 * Represents the aggregated result of evaluating a policy (set of specifications).
 *
 * @param allPassed whether all specifications passed
 * @param results the individual specification results
 * @param R the type of the failure reason (must be an enum)
 */
data class PolicyResult<R : Enum<R>>(
    val allPassed: Boolean,
    val results: List<SpecificationResult<R>>
) {

    /**
     * Returns only the failed specification results.
     */
    fun failedResults(): List<SpecificationResult<R>> =
        results.filter { !it.passed() }

    /**
     * Returns the failure reasons from failed specifications (flattened).
     */
    fun failureReasons(): List<R> =
        results.filter { !it.passed() }
            .flatMap { it.failureReasons ?: emptyList() }

    companion object {
        /**
         * Creates a successful evaluation with the given results.
         */
        internal fun <R : Enum<R>> success(results: List<SpecificationResult<R>>): PolicyResult<R> =
            PolicyResult(true, results.toList())

        /**
         * Creates a failed evaluation with the given results.
         */
        internal fun <R : Enum<R>> failure(results: List<SpecificationResult<R>>): PolicyResult<R> =
            PolicyResult(false, results.toList())
    }
}
