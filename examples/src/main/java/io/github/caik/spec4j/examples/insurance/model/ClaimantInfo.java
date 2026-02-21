package io.github.caik.spec4j.examples.insurance.model;

import java.time.LocalDate;

/**
 * Information about the person filing the claim.
 */
public record ClaimantInfo(
        String policyNumber,
        String name,
        LocalDate policyStartDate,
        LocalDate policyExpirationDate,
        PolicyTier tier,
        boolean isPolicyActive,
        boolean hasPaidPremiums
) {}

