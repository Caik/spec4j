package io.github.caik.spec4j.examples.insurance.reason;

/**
 * Reasons why an insurance claim may be denied.
 * 
 * <p>These are the failure reasons used by specifications when evaluating claims.
 * Each reason corresponds to a specific business rule violation.
 */
public enum ClaimDenialReason {
    // Policy-related
    POLICY_NOT_ACTIVE,
    POLICY_EXPIRED,
    PREMIUMS_NOT_PAID,
    
    // Amount-related
    CLAIM_EXCEEDS_COVERAGE,
    AMOUNT_NEGATIVE_OR_ZERO,
    
    // Timing-related
    INCIDENT_BEFORE_POLICY_START,
    CLAIM_FILED_TOO_LATE,
    
    // Documentation-related
    MISSING_REQUIRED_DOCUMENTS,
    DOCUMENTS_NOT_VERIFIED,
    
    // Fraud-related
    FLAGGED_FOR_FRAUD,
    
    // Review-related
    MANUAL_REVIEW_REQUIRED,
    MANUAL_REVIEW_REJECTED
}
