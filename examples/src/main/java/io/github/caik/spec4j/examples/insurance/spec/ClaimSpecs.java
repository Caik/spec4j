package io.github.caik.spec4j.examples.insurance.spec;

import io.github.caik.spec4j.Specification;
import io.github.caik.spec4j.SpecificationFactory;
import io.github.caik.spec4j.examples.insurance.model.Claim;
import io.github.caik.spec4j.examples.insurance.model.PolicyTier;
import io.github.caik.spec4j.examples.insurance.reason.ClaimDenialReason;

/**
 * Reusable specifications for insurance claim validation.
 * 
 * <p>This class demonstrates the pattern of organizing specifications
 * in a holder class for reuse across multiple policies.
 */
public final class ClaimSpecs {

    private ClaimSpecs() {
        // Utility class - prevent instantiation
    }

    // ========================================================================
    // Policy Status Specifications
    // ========================================================================

    /**
     * The policy must be currently active.
     */
    public static final Specification<Claim, ClaimDenialReason> POLICY_IS_ACTIVE =
            Specification.of(
                    "PolicyIsActive",
                    claim -> claim.claimant().isPolicyActive(),
                    ClaimDenialReason.POLICY_NOT_ACTIVE
            );

    /**
     * The policy must not be expired (expiration date is in the future).
     */
    public static final Specification<Claim, ClaimDenialReason> POLICY_NOT_EXPIRED =
            Specification.of(
                    "PolicyNotExpired",
                    claim -> claim.claimant().policyExpirationDate().isAfter(java.time.LocalDate.now()),
                    ClaimDenialReason.POLICY_EXPIRED
            );

    /**
     * All premiums must be paid up to date.
     */
    public static final Specification<Claim, ClaimDenialReason> PREMIUMS_PAID =
            Specification.of(
                    "PremiumsPaid",
                    claim -> claim.claimant().hasPaidPremiums(),
                    ClaimDenialReason.PREMIUMS_NOT_PAID
            );

    /**
     * Composite: All policy status requirements.
     */
    public static final Specification<Claim, ClaimDenialReason> VALID_POLICY_STATUS =
            SpecificationFactory.allOf(
                    "ValidPolicyStatus",
                    POLICY_IS_ACTIVE,
                    POLICY_NOT_EXPIRED,
                    PREMIUMS_PAID
            );

    // ========================================================================
    // Amount Specifications
    // ========================================================================

    /**
     * Claim amount must be positive.
     */
    public static final Specification<Claim, ClaimDenialReason> AMOUNT_IS_POSITIVE =
            Specification.of(
                    "AmountIsPositive",
                    claim -> claim.amount() > 0,
                    ClaimDenialReason.AMOUNT_NEGATIVE_OR_ZERO
            );

    /**
     * Creates a specification that checks if claim amount is within the tier's coverage limit.
     * 
     * <p>This demonstrates parameterized/dynamic specifications.
     * 
     * @param tier the policy tier to check against
     * @return a specification that validates the claim amount
     */
    public static Specification<Claim, ClaimDenialReason> amountWithinCoverage(PolicyTier tier) {
        return Specification.of(
                "AmountWithinCoverage(" + tier.name() + ")",
                claim -> claim.amount() <= tier.getMaxClaimAmount(),
                ClaimDenialReason.CLAIM_EXCEEDS_COVERAGE
        );
    }

    /**
     * Checks if the claim amount is within the claimant's policy tier coverage.
     */
    public static final Specification<Claim, ClaimDenialReason> AMOUNT_WITHIN_COVERAGE =
            Specification.of(
                    "AmountWithinCoverage",
                    claim -> claim.amount() <= claim.claimant().tier().getMaxClaimAmount(),
                    ClaimDenialReason.CLAIM_EXCEEDS_COVERAGE
            );

    // ========================================================================
    // Timing Specifications
    // ========================================================================

    /**
     * Incident must have occurred after the policy start date.
     */
    public static final Specification<Claim, ClaimDenialReason> INCIDENT_AFTER_POLICY_START =
            Specification.of(
                    "IncidentAfterPolicyStart",
                    claim -> !claim.incidentDate().isBefore(claim.claimant().policyStartDate()),
                    ClaimDenialReason.INCIDENT_BEFORE_POLICY_START
            );

    /**
     * Creates a specification that checks if the claim was filed within the allowed period.
     * 
     * @param maxDays maximum days allowed between incident and filing
     * @return a specification that validates filing timeliness
     */
    public static Specification<Claim, ClaimDenialReason> filedWithinDays(int maxDays) {
        return Specification.of(
                "FiledWithin" + maxDays + "Days",
                claim -> claim.daysSinceIncident() <= maxDays,
                ClaimDenialReason.CLAIM_FILED_TOO_LATE
        );
    }

    // ========================================================================
    // Fraud Prevention
    // ========================================================================

    /**
     * Claim must NOT be flagged for fraud.
     * 
     * <p>This demonstrates using not() to negate a specification.
     */
    public static final Specification<Claim, ClaimDenialReason> NOT_FLAGGED_FOR_FRAUD =
            SpecificationFactory.not(
                    "NotFlaggedForFraud",
                    ClaimDenialReason.FLAGGED_FOR_FRAUD,
                    Specification.of(
                            "IsFlaggedForFraud",
                            Claim::flaggedForFraud,
                            ClaimDenialReason.FLAGGED_FOR_FRAUD
                    )
            );

    // ========================================================================
    // Approval Path Specifications
    // ========================================================================

    /**
     * Claim is small enough for auto-approval (under $1000).
     */
    public static final Specification<Claim, ClaimDenialReason> IS_SMALL_CLAIM =
            Specification.of(
                    "IsSmallClaim",
                    Claim::isSmallClaim,
                    ClaimDenialReason.MANUAL_REVIEW_REQUIRED
            );

    /**
     * Claim has been manually reviewed and approved.
     */
    public static final Specification<Claim, ClaimDenialReason> MANUAL_REVIEW_APPROVED =
            Specification.of(
                    "ManualReviewApproved",
                    Claim::manualReviewApproved,
                    ClaimDenialReason.MANUAL_REVIEW_REJECTED
            );

    /**
     * Claim can be approved if EITHER it's a small claim OR manual review passed.
     * 
     * <p>This demonstrates anyOf() for alternative approval paths.
     */
    public static final Specification<Claim, ClaimDenialReason> APPROVAL_PATH_SATISFIED =
            SpecificationFactory.anyOf(
                    "ApprovalPathSatisfied",
                    IS_SMALL_CLAIM,
                    MANUAL_REVIEW_APPROVED
            );
}

