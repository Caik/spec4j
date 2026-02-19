package io.github.caik.spec4j

/**
 * Represents a specification (single condition) that can be evaluated against a context.
 *
 * @param T the type of the context to evaluate
 * @param R the type of the failure reason (must be an enum)
 */
fun interface Specification<T, R : Enum<R>> {
    /**
     * Evaluates this specification against the given context.
     *
     * @param context the context to evaluate
     * @return the result of the evaluation
     */
    fun evaluate(context: T): SpecificationResult<R>

    /**
     * Returns the name of this specification. Defaults to the simple class name.
     */
    fun name(): String = this::class.simpleName ?: "Unknown"

    companion object {
        /**
         * Creates a specification from a name, predicate, and failure reason.
         *
         * @param name the specification name (for audit/debugging)
         * @param predicate the condition that must be true for the specification to pass
         * @param failureReason the reason returned when the specification fails
         * @return a new specification
         */
        fun <T, R : Enum<R>> of(
            name: String,
            predicate: (T) -> Boolean,
            failureReason: R,
        ): Specification<T, R> =
            Specification { context ->
                if (predicate(context)) {
                    SpecificationResult.pass(name)
                } else {
                    SpecificationResult.fail(name, failureReason)
                }
            }
    }
}
