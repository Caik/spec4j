package io.github.caik.spec4j.examples.insurance.model;

/**
 * Insurance policy tiers with different coverage limits.
 */
public enum PolicyTier {
    BASIC(10_000.0),
    STANDARD(50_000.0),
    PREMIUM(200_000.0),
    PLATINUM(1_000_000.0);

    private final double maxClaimAmount;

    PolicyTier(double maxClaimAmount) {
        this.maxClaimAmount = maxClaimAmount;
    }

    public double getMaxClaimAmount() {
        return maxClaimAmount;
    }
}

