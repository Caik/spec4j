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
     * Evaluates this specification against the given context.
     * Kotlin operator that allows `spec(context)` syntax instead of `spec.evaluate(context)`.
     *
     * @param context the context to evaluate
     * @return the result of the evaluation
     */
    operator fun invoke(context: T): SpecificationResult<R> = evaluate(context)

    /**
     * Returns the name of this specification. Defaults to the simple class name.
     */
    fun name(): String = this::class.simpleName ?: "Unknown"

    /**
     * Returns a SQL-like human-readable expression representing this specification's structure.
     * For simple specs, returns the name. For composites, shows the logical structure
     * (e.g., "AgeCheck AND (HasIncome OR HasAssets)").
     *
     * @return the expression string
     */
    fun toExpression(): String = name()

    companion object {
        /**
         * Creates a specification from a name, predicate, and failure reason.
         *
         * @param name the specification name (for audit/debugging)
         * @param predicate the condition that must be true for the specification to pass
         * @param failureReason the reason returned when the specification fails
         * @return a new specification
         */
        @JvmStatic
        fun <T, R : Enum<R>> of(
            name: String,
            predicate: (T) -> Boolean,
            failureReason: R,
        ): Specification<T, R> =
            object : Specification<T, R> {
                override fun evaluate(context: T): SpecificationResult<R> =
                    if (predicate(context)) {
                        SpecificationResult.pass(name)
                    } else {
                        SpecificationResult.fail(name, failureReason)
                    }

                override fun toExpression(): String = name
            }
    }
}
