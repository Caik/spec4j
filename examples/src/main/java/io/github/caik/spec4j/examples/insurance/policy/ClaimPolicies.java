package io.github.caik.spec4j.examples.insurance.policy;

import io.github.caik.spec4j.Policy;
import io.github.caik.spec4j.examples.insurance.model.Claim;
import io.github.caik.spec4j.examples.insurance.reason.ClaimDenialReason;
import io.github.caik.spec4j.examples.insurance.spec.ClaimSpecs;
import io.github.caik.spec4j.examples.insurance.spec.DocumentComplianceSpec;

/**
 * Policy definitions for different types of insurance claims.
 * 
 * <p>This demonstrates:
 * <ul>
 *   <li>Multiple policies sharing common specifications</li>
 *   <li>Different policies for different claim types</li>
 *   <li>Composition of specifications into complete business rules</li>
 * </ul>
 */
public final class ClaimPolicies {

    private ClaimPolicies() {
        // Utility class - prevent instantiation
    }

    /**
     * Document compliance specification (shared across all policies).
     */
    private static final DocumentComplianceSpec DOCUMENT_COMPLIANCE = new DocumentComplianceSpec();

    // ========================================================================
    // Base Policy (common to all claim types)
    // ========================================================================

    /**
     * Creates a base policy with rules common to all claim types.
     * This is used as a starting point for type-specific policies.
     */
    private static Policy<Claim, ClaimDenialReason> createBasePolicy() {
        return Policy.<Claim, ClaimDenialReason>create()
                .with(ClaimSpecs.VALID_POLICY_STATUS)      // Policy must be active, not expired, premiums paid
                .with(ClaimSpecs.AMOUNT_IS_POSITIVE)       // Amount must be > 0
                .with(ClaimSpecs.AMOUNT_WITHIN_COVERAGE)   // Amount within tier limits
                .with(ClaimSpecs.NOT_FLAGGED_FOR_FRAUD);   // Not flagged for fraud
    }

    // ========================================================================
    // Medical Claims Policy
    // ========================================================================

    /**
     * Policy for medical insurance claims.
     * 
     * <p>Medical claims have a 90-day filing window and require
     * medical records documentation.
     */
    public static final Policy<Claim, ClaimDenialReason> MEDICAL_CLAIM_POLICY =
            createBasePolicy()
                    .with(ClaimSpecs.INCIDENT_AFTER_POLICY_START)
                    .with(ClaimSpecs.filedWithinDays(90))  // 90-day filing window
                    .with(DOCUMENT_COMPLIANCE)
                    .with(ClaimSpecs.APPROVAL_PATH_SATISFIED);

    // ========================================================================
    // Auto Claims Policy
    // ========================================================================

    /**
     * Policy for auto insurance claims.
     * 
     * <p>Auto claims have a 30-day filing window and require
     * police report, photos, and repair estimates.
     */
    public static final Policy<Claim, ClaimDenialReason> AUTO_CLAIM_POLICY =
            createBasePolicy()
                    .with(ClaimSpecs.INCIDENT_AFTER_POLICY_START)
                    .with(ClaimSpecs.filedWithinDays(30))  // 30-day filing window (stricter)
                    .with(DOCUMENT_COMPLIANCE)
                    .with(ClaimSpecs.APPROVAL_PATH_SATISFIED);

    // ========================================================================
    // Property Claims Policy
    // ========================================================================

    /**
     * Policy for property insurance claims.
     * 
     * <p>Property claims have a 60-day filing window and require
     * proof of ownership, photos, and repair estimates.
     */
    public static final Policy<Claim, ClaimDenialReason> PROPERTY_CLAIM_POLICY =
            createBasePolicy()
                    .with(ClaimSpecs.INCIDENT_AFTER_POLICY_START)
                    .with(ClaimSpecs.filedWithinDays(60))  // 60-day filing window
                    .with(DOCUMENT_COMPLIANCE)
                    .with(ClaimSpecs.APPROVAL_PATH_SATISFIED);

    // ========================================================================
    // Life Insurance Claims Policy
    // ========================================================================

    /**
     * Policy for life insurance claims.
     * 
     * <p>Life insurance claims have a 365-day filing window and require
     * death certificate. They always require manual review (no auto-approval).
     */
    public static final Policy<Claim, ClaimDenialReason> LIFE_CLAIM_POLICY =
            createBasePolicy()
                    .with(ClaimSpecs.INCIDENT_AFTER_POLICY_START)
                    .with(ClaimSpecs.filedWithinDays(365)) // 1-year filing window
                    .with(DOCUMENT_COMPLIANCE)
                    .with(ClaimSpecs.MANUAL_REVIEW_APPROVED); // Always requires manual review

    // ========================================================================
    // Policy Selection
    // ========================================================================

    /**
     * Returns the appropriate policy for a given claim based on its type.
     * 
     * @param claim the claim to get a policy for
     * @return the policy applicable to this claim type
     */
    public static Policy<Claim, ClaimDenialReason> getPolicyFor(Claim claim) {
        return switch (claim.type()) {
            case MEDICAL -> MEDICAL_CLAIM_POLICY;
            case AUTO -> AUTO_CLAIM_POLICY;
            case PROPERTY -> PROPERTY_CLAIM_POLICY;
            case LIFE -> LIFE_CLAIM_POLICY;
        };
    }
}

