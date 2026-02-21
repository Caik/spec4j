package io.github.caik.spec4j.examples.insurance.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents an insurance claim to be evaluated.
 * This is the "context" object that specifications evaluate.
 */
public record Claim(
        String claimId,
        ClaimType type,
        double amount,
        LocalDate incidentDate,
        LocalDate filingDate,
        ClaimantInfo claimant,
        List<Document> documents,
        boolean flaggedForFraud,
        boolean manualReviewApproved,
        String notes
) {
    /**
     * Returns the number of days between incident and filing.
     */
    public long daysSinceIncident() {
        return java.time.temporal.ChronoUnit.DAYS.between(incidentDate, filingDate);
    }

    /**
     * Checks if the claim qualifies for auto-approval (small claims).
     */
    public boolean isSmallClaim() {
        return amount <= 1000.0;
    }
}

