package io.github.caik.spec4j.examples.accesscontrol

import io.github.caik.spec4j.Policy
import io.github.caik.spec4j.Specification
import io.github.caik.spec4j.SpecificationFactory

/**
 * Feature Access Control Example
 *
 * Demonstrates:
 * - Multiple policies sharing the same specifications
 * - Dynamic/conditional specifications
 * - Role-based access patterns
 * - Combining specs for different feature tiers
 */

// ============================================================================
// Domain Model
// ============================================================================

enum class AccessDeniedReason {
    NOT_AUTHENTICATED,
    ACCOUNT_SUSPENDED,
    INSUFFICIENT_ROLE,
    FEATURE_NOT_IN_PLAN,
    TRIAL_EXPIRED,
    RATE_LIMIT_EXCEEDED,
    REGION_RESTRICTED,
    MAINTENANCE_MODE
}

enum class Role { GUEST, USER, PREMIUM, ADMIN }
enum class Plan { FREE, BASIC, PRO, ENTERPRISE }

data class AccessContext(
    val authenticated: Boolean,
    val suspended: Boolean,
    val role: Role,
    val plan: Plan,
    val trialDaysRemaining: Int,
    val requestsThisMinute: Int,
    val region: String,
    val maintenanceMode: Boolean
)

// ============================================================================
// Shared Specifications (reused across multiple policies)
// ============================================================================

object AccessSpecs {

    val isAuthenticated = Specification.of<AccessContext, AccessDeniedReason>(
        "IsAuthenticated",
        { it.authenticated },
        AccessDeniedReason.NOT_AUTHENTICATED
    )

    val isNotSuspended = Specification.of<AccessContext, AccessDeniedReason>(
        "IsNotSuspended",
        { !it.suspended },
        AccessDeniedReason.ACCOUNT_SUSPENDED
    )

    val isNotInMaintenance = Specification.of<AccessContext, AccessDeniedReason>(
        "IsNotInMaintenance",
        { !it.maintenanceMode },
        AccessDeniedReason.MAINTENANCE_MODE
    )

    // Role-based specifications
    fun hasMinimumRole(minRole: Role) = Specification.of<AccessContext, AccessDeniedReason>(
        "HasMinimumRole($minRole)",
        { it.role.ordinal >= minRole.ordinal },
        AccessDeniedReason.INSUFFICIENT_ROLE
    )

    // Plan-based specifications
    fun hasMinimumPlan(minPlan: Plan) = Specification.of<AccessContext, AccessDeniedReason>(
        "HasMinimumPlan($minPlan)",
        { it.plan.ordinal >= minPlan.ordinal },
        AccessDeniedReason.FEATURE_NOT_IN_PLAN
    )

    val hasActiveTrial = Specification.of<AccessContext, AccessDeniedReason>(
        "HasActiveTrial",
        { it.trialDaysRemaining > 0 },
        AccessDeniedReason.TRIAL_EXPIRED
    )

    // Rate limiting with configurable threshold
    fun withinRateLimit(maxRequests: Int) = Specification.of<AccessContext, AccessDeniedReason>(
        "WithinRateLimit($maxRequests)",
        { it.requestsThisMinute < maxRequests },
        AccessDeniedReason.RATE_LIMIT_EXCEEDED
    )

    // Region restrictions
    fun allowedRegion(allowedRegions: Set<String>) = Specification.of<AccessContext, AccessDeniedReason>(
        "AllowedRegion",
        { it.region in allowedRegions },
        AccessDeniedReason.REGION_RESTRICTED
    )

    // Dynamic specification: Plan OR active trial
    val hasPaidAccessOrTrial = SpecificationFactory.anyOf(
        "HasPaidAccessOrTrial",
        hasMinimumPlan(Plan.BASIC),
        hasActiveTrial
    )
}

// ============================================================================
// Multiple Policies for Different Features
// ============================================================================

object FeaturePolicies {

    // Basic feature: just needs authentication
    val basicFeatureAccess = Policy.create<AccessContext, AccessDeniedReason>()
        .with(AccessSpecs.isNotInMaintenance)
        .with(AccessSpecs.isAuthenticated)
        .with(AccessSpecs.isNotSuspended)

    // Premium feature: needs paid plan or trial + higher rate limit
    val premiumFeatureAccess = Policy.create<AccessContext, AccessDeniedReason>()
        .with(AccessSpecs.isNotInMaintenance)
        .with(AccessSpecs.isAuthenticated)
        .with(AccessSpecs.isNotSuspended)
        .with(AccessSpecs.hasPaidAccessOrTrial)
        .with(AccessSpecs.withinRateLimit(100))

    // Admin feature: role-based + region restricted
    val adminFeatureAccess = Policy.create<AccessContext, AccessDeniedReason>()
        .with(AccessSpecs.isNotInMaintenance)
        .with(AccessSpecs.isAuthenticated)
        .with(AccessSpecs.isNotSuspended)
        .with(AccessSpecs.hasMinimumRole(Role.ADMIN))
        .with(AccessSpecs.allowedRegion(setOf("US", "EU", "UK")))

    // API access: different rate limits per plan
    fun apiAccess(plan: Plan): Policy<AccessContext, AccessDeniedReason> {
        val rateLimit = when (plan) {
            Plan.FREE -> 10
            Plan.BASIC -> 60
            Plan.PRO -> 300
            Plan.ENTERPRISE -> 1000
        }
        return Policy.create<AccessContext, AccessDeniedReason>()
            .with(AccessSpecs.isAuthenticated)
            .with(AccessSpecs.isNotSuspended)
            .with(AccessSpecs.withinRateLimit(rateLimit))
    }
}

// ============================================================================
// Main Example
// ============================================================================

fun main() {
    println("=== Feature Access Control Example ===\n")

    val normalUser = AccessContext(
        authenticated = true,
        suspended = false,
        role = Role.USER,
        plan = Plan.BASIC,
        trialDaysRemaining = 0,
        requestsThisMinute = 5,
        region = "US",
        maintenanceMode = false
    )

    val trialUser = normalUser.copy(plan = Plan.FREE, trialDaysRemaining = 14)
    val expiredTrial = normalUser.copy(plan = Plan.FREE, trialDaysRemaining = 0)
    val adminUser = normalUser.copy(role = Role.ADMIN, plan = Plan.ENTERPRISE)
    val suspendedUser = normalUser.copy(suspended = true)
    val rateLimitedUser = normalUser.copy(requestsThisMinute = 150)

    println("--- Basic Feature Access ---")
    runTest("Normal user", normalUser, FeaturePolicies.basicFeatureAccess)
    runTest("Suspended user", suspendedUser, FeaturePolicies.basicFeatureAccess)

    println("--- Premium Feature Access ---")
    runTest("Paid user (BASIC plan)", normalUser, FeaturePolicies.premiumFeatureAccess)
    runTest("Trial user (14 days left)", trialUser, FeaturePolicies.premiumFeatureAccess)
    runTest("Expired trial", expiredTrial, FeaturePolicies.premiumFeatureAccess)
    runTest("Rate limited user", rateLimitedUser, FeaturePolicies.premiumFeatureAccess)

    println("--- Admin Feature Access ---")
    runTest("Admin user in US", adminUser, FeaturePolicies.adminFeatureAccess)
    runTest("Admin in restricted region", adminUser.copy(region = "CN"), FeaturePolicies.adminFeatureAccess)
    runTest("Normal user (not admin)", normalUser, FeaturePolicies.adminFeatureAccess)

    val heavyUser = normalUser.copy(requestsThisMinute = 50)

    println("--- Dynamic API Rate Limits by Plan ---")
    runTest("FREE plan (limit 10)", heavyUser.copy(plan = Plan.FREE), FeaturePolicies.apiAccess(Plan.FREE))
    runTest("BASIC plan (limit 60)", heavyUser.copy(plan = Plan.BASIC), FeaturePolicies.apiAccess(Plan.BASIC))
    runTest("PRO plan (limit 300)", heavyUser.copy(plan = Plan.PRO), FeaturePolicies.apiAccess(Plan.PRO))

    val maintenanceContext = adminUser.copy(maintenanceMode = true)

    println("--- Maintenance Mode (blocks everything) ---")
    runTest("Admin during maintenance", maintenanceContext, FeaturePolicies.adminFeatureAccess)
}

private fun runTest(name: String, context: AccessContext, policy: Policy<AccessContext, AccessDeniedReason>) {
    val result = policy.evaluateFailFast(context)
    print("$name: ")

    if (result.allPassed) {
        println("✅ ACCESS GRANTED")
    } else {
        println("❌ DENIED - ${result.failureReasons()}")
    }
}
