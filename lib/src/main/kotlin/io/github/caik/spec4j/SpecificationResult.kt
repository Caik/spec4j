package io.github.caik.spec4j

/**
 * Result of evaluating a single specification.
 *
 * @param name the name of the specification (for audit/debugging)
 * @param failureReasons the reasons for failure (null if passed)
 * @param R the type of the failure reason (must be an enum)
 */
data class SpecificationResult<R : Enum<R>>(
    val name: String,
    val failureReasons: List<R>? = null
) {

    /**
     * Returns whether the specification passed (no failure reasons).
     */
    fun passed(): Boolean = failureReasons.isNullOrEmpty()

    companion object {
        /**
         * Creates a passing result.
         *
         * @param name the name of the specification
         * @return a passing result
         */
        fun <R : Enum<R>> pass(name: String): SpecificationResult<R> =
            SpecificationResult(name, null)

        /**
         * Creates a failing result with one or more reasons.
         *
         * @param name the name of the specification
         * @param failureReasons the reasons for failure
         * @return a failing result
         */
        fun <R : Enum<R>> fail(name: String, vararg failureReasons: R): SpecificationResult<R> =
            SpecificationResult(name, failureReasons.toList())

        /**
         * Creates a failing result with a list of reasons.
         *
         * @param name the name of the specification
         * @param failureReasons the reasons for failure
         * @return a failing result
         */
        fun <R : Enum<R>> fail(name: String, failureReasons: List<R>): SpecificationResult<R> =
            SpecificationResult(name, failureReasons.toList())
    }
}
