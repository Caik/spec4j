package io.github.caik.spec4j.examples.ecommerce

import io.github.caik.spec4j.Policy
import io.github.caik.spec4j.Specification
import io.github.caik.spec4j.SpecificationFactory
import io.github.caik.spec4j.SpecificationResult

/**
 * E-commerce Order Validation Example
 *
 * Demonstrates:
 * - Custom Specification implementations (not just Specification.of())
 * - The not() composite operator
 * - Reusable specification objects
 * - Multiple failure reasons from a single spec
 */

enum class OrderFailureReason {
    EMPTY_CART,
    EXCEEDS_MAX_ITEMS,
    ITEM_OUT_OF_STOCK,
    INVALID_SHIPPING_ADDRESS,
    BLOCKED_COUNTRY,
    PAYMENT_METHOD_EXPIRED,
    INSUFFICIENT_FUNDS,
    FRAUD_SUSPECTED,
}

data class OrderContext(
    val items: List<OrderItem>,
    val shippingAddress: ShippingAddress?,
    val paymentMethod: PaymentMethod?,
    /** Risk score: 0.0 = safe, 1.0 = high risk */
    val customerRiskScore: Double,
)

data class OrderItem(val sku: String, val quantity: Int, val inStock: Boolean)

data class ShippingAddress(val country: String, val isValid: Boolean)

data class PaymentMethod(val type: String, val expired: Boolean, val availableBalance: Double)

// ============================================================================
// Custom Specification: Validates all items are in stock (multiple failures)
// ============================================================================

/**
 * A custom Specification implementation that checks stock for ALL items
 * and reports each out-of-stock item separately.
 *
 * This demonstrates implementing the Specification interface directly
 * rather than using Specification.of() - useful for complex validation logic.
 */
class AllItemsInStockSpec : Specification<OrderContext, OrderFailureReason> {
    override fun name(): String = "AllItemsInStock"

    override fun evaluate(context: OrderContext): SpecificationResult<OrderFailureReason> {
        val outOfStockItems = context.items.filter { !it.inStock }

        return if (outOfStockItems.isEmpty()) {
            SpecificationResult.pass(name())
        } else {
            // Report one ITEM_OUT_OF_STOCK reason per out-of-stock item
            SpecificationResult.fail(
                name(),
                outOfStockItems.map { OrderFailureReason.ITEM_OUT_OF_STOCK },
            )
        }
    }
}

// ============================================================================
// Reusable Specifications Object
// ============================================================================

object OrderSpecs {
    val hasItems =
        Specification.of<OrderContext, OrderFailureReason>(
            "HasItems",
            { it.items.isNotEmpty() },
            OrderFailureReason.EMPTY_CART,
        )

    val withinMaxItems =
        Specification.of<OrderContext, OrderFailureReason>(
            "WithinMaxItems",
            { it.items.sumOf { item -> item.quantity } <= 100 },
            OrderFailureReason.EXCEEDS_MAX_ITEMS,
        )

    val allItemsInStock = AllItemsInStockSpec()

    val hasValidAddress =
        Specification.of<OrderContext, OrderFailureReason>(
            "HasValidAddress",
            { it.shippingAddress?.isValid == true },
            OrderFailureReason.INVALID_SHIPPING_ADDRESS,
        )

    // Specification that will be negated: "is blocked country"
    // The BLOCKED_COUNTRY reason is used when NOT is applied
    val isBlockedCountry =
        Specification.of<OrderContext, OrderFailureReason>(
            "IsBlockedCountry",
            { it.shippingAddress?.country in listOf("XX", "YY", "ZZ") },
            OrderFailureReason.BLOCKED_COUNTRY,
        )

    val hasValidPayment =
        Specification.of<OrderContext, OrderFailureReason>(
            "HasValidPayment",
            { it.paymentMethod?.expired == false },
            OrderFailureReason.PAYMENT_METHOD_EXPIRED,
        )

    val hasSufficientFunds =
        Specification.of<OrderContext, OrderFailureReason>(
            "HasSufficientFunds",
            { (it.paymentMethod?.availableBalance ?: 0.0) >= 10.0 },
            OrderFailureReason.INSUFFICIENT_FUNDS,
        )

    val isLowRisk =
        Specification.of<OrderContext, OrderFailureReason>(
            "IsLowRisk",
            { it.customerRiskScore < 0.7 },
            OrderFailureReason.FRAUD_SUSPECTED,
        )
}

// ============================================================================
// Main Example
// ============================================================================

fun main() {
    println("=== E-commerce Order Validation Example ===\n")

    // Build policy using not() to negate "is blocked country"
    val orderValidationPolicy =
        Policy.create<OrderContext, OrderFailureReason>()
            .with(OrderSpecs.hasItems)
            .with(OrderSpecs.withinMaxItems)
            .with(OrderSpecs.allItemsInStock)
            .with(OrderSpecs.hasValidAddress)
            .with(SpecificationFactory.not("NotBlockedCountry", OrderFailureReason.BLOCKED_COUNTRY, OrderSpecs.isBlockedCountry))
            .with(SpecificationFactory.allOf("PaymentValid", OrderSpecs.hasValidPayment, OrderSpecs.hasSufficientFunds))
            .with(OrderSpecs.isLowRisk)

    // Test cases
    val validOrder =
        OrderContext(
            items = listOf(OrderItem("SKU-001", 2, true), OrderItem("SKU-002", 1, true)),
            shippingAddress = ShippingAddress("US", true),
            paymentMethod = PaymentMethod("CREDIT", false, 500.0),
            customerRiskScore = 0.1,
        )

    runTest("Valid order", validOrder, orderValidationPolicy)

    val emptyCart = validOrder.copy(items = emptyList())
    runTest("Empty cart", emptyCart, orderValidationPolicy)

    // Create order with some out of stock items
    val outOfStockOrder =
        OrderContext(
            items =
                listOf(
                    OrderItem("SKU-001", 2, false),
                    OrderItem("SKU-002", 1, true),
                    OrderItem("SKU-003", 3, false),
                ),
            shippingAddress = ShippingAddress("US", true),
            paymentMethod = PaymentMethod("CREDIT", false, 500.0),
            customerRiskScore = 0.2,
        )

    runTest("Multiple items out of stock", outOfStockOrder, orderValidationPolicy)

    // XX is a blocked country
    val blockedCountry =
        validOrder.copy(
            shippingAddress = ShippingAddress("XX", true),
        )

    runTest("Blocked country (using not())", blockedCountry, orderValidationPolicy)

    val fraudSuspected = validOrder.copy(customerRiskScore = 0.9)
    runTest("High risk customer", fraudSuspected, orderValidationPolicy)

    // Demonstrate evaluateAll - collects all failures
    val multipleIssues =
        OrderContext(
            items = listOf(OrderItem("SKU-001", 1, false)),
            shippingAddress = ShippingAddress("XX", false),
            paymentMethod = PaymentMethod("CREDIT", true, 5.0),
            customerRiskScore = 0.95,
        )

    println("\n--- Using evaluateAll (collects all failures) ---")
    val allResults = orderValidationPolicy.evaluateAll(multipleIssues)

    println("Multiple issues order:")
    println("  All failure reasons: ${allResults.failureReasons()}")
    println("  Failed specs: ${allResults.failedResults().map { it.name }}")
}

private fun runTest(
    name: String,
    context: OrderContext,
    policy: Policy<OrderContext, OrderFailureReason>,
) {
    val result = policy.evaluateFailFast(context)

    println("$name:")
    println("  Items: ${context.items.size}, Address: ${context.shippingAddress?.country}, Risk: ${context.customerRiskScore}")

    if (result.allPassed) {
        println("  Result: ✅ VALID")
    } else {
        println("  Result: ❌ INVALID - ${result.failureReasons()}")
    }

    println()
}
