package io.github.caik.spec4j

/**
 * Factory methods for creating composite specifications.
 */
object SpecificationFactory {
    /**
     * Creates a specification that passes if ANY of the given specifications pass.
     * Short-circuits on first passing specification.
     */
    @JvmStatic
    @SafeVarargs
    fun <T, R : Enum<R>> anyOf(
        name: String,
        vararg specifications: Specification<T, R>,
    ): Specification<T, R> = anyOf(name, false, specifications.toList())

    /**
     * Creates a specification that passes if ANY of the given specifications pass.
     */
    @JvmStatic
    fun <T, R : Enum<R>> anyOf(
        name: String,
        specifications: List<Specification<T, R>>,
    ): Specification<T, R> = anyOf(name, false, specifications)

    /**
     * Creates a specification that passes if ANY of the given specifications pass.
     */
    @JvmStatic
    @SafeVarargs
    fun <T, R : Enum<R>> anyOf(
        name: String,
        evaluateAll: Boolean,
        vararg specifications: Specification<T, R>,
    ): Specification<T, R> = anyOf(name, evaluateAll, specifications.toList())

    /**
     * Creates a specification that passes if ANY of the given specifications pass.
     *
     * @param name the composite specification name
     * @param evaluateAll if true, evaluates all specifications even if one passes
     * @param specifications the specifications to evaluate
     * @return a composite specification
     */
    @JvmStatic
    fun <T, R : Enum<R>> anyOf(
        name: String,
        evaluateAll: Boolean,
        specifications: List<Specification<T, R>>,
    ): Specification<T, R> {
        require(specifications.isNotEmpty()) { "anyOf requires at least one specification" }

        val expression =
            if (specifications.size > 1) {
                "(${specifications.joinToString(" OR ") { it.toExpression() }})"
            } else {
                specifications.first().toExpression()
            }

        return object : Specification<T, R> {
            override fun evaluate(context: T): SpecificationResult<R> {
                val failureReasons = mutableListOf<R>()
                var anyPassed = false

                for (spec in specifications) {
                    val result = spec.evaluate(context)

                    if (result.passed() && !evaluateAll) {
                        return SpecificationResult.pass(name)
                    }

                    if (result.passed()) {
                        anyPassed = true
                        continue
                    }

                    result.failureReasons?.let { failureReasons.addAll(it) }
                }

                return if (anyPassed) {
                    SpecificationResult.pass(name)
                } else {
                    SpecificationResult.fail(name, failureReasons)
                }
            }

            override fun toExpression(): String = expression
        }
    }

    /**
     * Creates a specification that passes if ALL the given specifications pass.
     */
    @JvmStatic
    @SafeVarargs
    fun <T, R : Enum<R>> allOf(
        name: String,
        vararg specifications: Specification<T, R>,
    ): Specification<T, R> = allOf(name, specifications.toList())

    /**
     * Creates a specification that passes if ALL the given specifications pass.
     */
    @JvmStatic
    fun <T, R : Enum<R>> allOf(
        name: String,
        specifications: List<Specification<T, R>>,
    ): Specification<T, R> {
        require(specifications.isNotEmpty()) { "allOf requires at least one specification" }

        val expression =
            if (specifications.size > 1) {
                "(${specifications.joinToString(" AND ") { it.toExpression() }})"
            } else {
                specifications.first().toExpression()
            }

        return object : Specification<T, R> {
            override fun evaluate(context: T): SpecificationResult<R> {
                val failureReasons = mutableListOf<R>()

                for (spec in specifications) {
                    val result = spec.evaluate(context)

                    if (!result.passed()) {
                        result.failureReasons?.let { failureReasons.addAll(it) }
                    }
                }

                return if (failureReasons.isEmpty()) {
                    SpecificationResult.pass(name)
                } else {
                    SpecificationResult.fail(name, failureReasons)
                }
            }

            override fun toExpression(): String = expression
        }
    }

    /**
     * Creates a specification that inverts the result of the given specification.
     */
    @JvmStatic
    fun <T, R : Enum<R>> not(
        name: String,
        failureReason: R,
        specification: Specification<T, R>,
    ): Specification<T, R> {
        val expression = "NOT ${specification.toExpression()}"

        return object : Specification<T, R> {
            override fun evaluate(context: T): SpecificationResult<R> {
                val result = specification.evaluate(context)
                return if (result.passed()) {
                    SpecificationResult.fail(name, failureReason)
                } else {
                    SpecificationResult.pass(name)
                }
            }

            override fun toExpression(): String = expression
        }
    }
}
