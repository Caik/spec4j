package io.github.caik.spec4j.examples.insurance;

import io.github.caik.spec4j.Policy;
import io.github.caik.spec4j.PolicyResult;
import io.github.caik.spec4j.examples.insurance.model.*;
import io.github.caik.spec4j.examples.insurance.policy.ClaimPolicies;
import io.github.caik.spec4j.examples.insurance.reason.ClaimDenialReason;

import java.time.LocalDate;
import java.util.List;

/**
 * Insurance Claim Processing Example
 *
 * <p>This example demonstrates the full power of the Specification/Policy framework from Java:
 * <ul>
 *   <li>Custom {@code Specification} implementations (DocumentComplianceSpec)</li>
 *   <li>Composite specifications using {@code allOf()}, {@code anyOf()}, {@code not()}</li>
 *   <li>Parameterized/dynamic specifications (filing windows, coverage limits)</li>
 *   <li>Multiple policies for different claim types</li>
 *   <li>Reusable specification holder classes</li>
 *   <li>Both evaluation modes: fail-fast and evaluate-all</li>
 * </ul>
 */
public class InsuranceClaimExample {

    public static void main(String[] args) {
        System.out.println("=== Insurance Claim Processing Example ===\n");

        // ====================================================================
        // Test Data Setup
        // ====================================================================

        // Active claimant with good standing
        ClaimantInfo activeClaimant = new ClaimantInfo(
                "POL-12345",
                "John Smith",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyTier.STANDARD,
                true,
                true
        );

        // Expired policy claimant
        ClaimantInfo expiredClaimant = new ClaimantInfo(
                "POL-99999",
                "Jane Doe",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2023, 12, 31),
                PolicyTier.BASIC,
                false,
                true
        );

        // ====================================================================
        // Medical Claim Tests
        // ====================================================================

        System.out.println("--- Medical Claims (90-day filing window) ---\n");

        // Valid medical claim (small, auto-approved)
        Claim validMedical = new Claim(
                "CLM-001",
                ClaimType.MEDICAL,
                500.0,  // Small claim - auto-approval eligible
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.MEDICAL_RECORDS, true)
                ),
                false,
                false,
                "Routine medical expense"
        );
        runTest("Valid small medical claim (auto-approved)", validMedical);

        // Medical claim needing manual review (larger amount)
        Claim largeMedical = new Claim(
                "CLM-002",
                ClaimType.MEDICAL,
                15000.0,  // Large claim - needs manual review
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.MEDICAL_RECORDS, true)
                ),
                false,
                true,  // Manual review approved
                "Surgery expenses"
        );
        runTest("Large medical claim (manual review approved)", largeMedical);

        // Medical claim - manual review rejected
        Claim rejectedMedical = new Claim(
                "CLM-003",
                ClaimType.MEDICAL,
                15000.0,
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.MEDICAL_RECORDS, true)
                ),
                false,
                false,  // Manual review NOT approved
                "Surgery expenses"
        );
        runTest("Large medical claim (manual review pending)", rejectedMedical);

        // ====================================================================
        // Auto Claim Tests
        // ====================================================================

        System.out.println("--- Auto Claims (30-day filing window) ---\n");

        // Valid auto claim
        Claim validAuto = new Claim(
                "CLM-010",
                ClaimType.AUTO,
                800.0,
                LocalDate.now().minusDays(15),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.POLICE_REPORT, true),
                        new Document(DocumentType.PHOTOS, true),
                        new Document(DocumentType.REPAIR_ESTIMATE, true)
                ),
                false,
                false,
                "Fender bender"
        );
        runTest("Valid auto claim", validAuto);

        // Auto claim filed too late
        Claim lateAuto = new Claim(
                "CLM-011",
                ClaimType.AUTO,
                5000.0,
                LocalDate.now().minusDays(45),  // Incident 45 days ago
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.POLICE_REPORT, true),
                        new Document(DocumentType.PHOTOS, true),
                        new Document(DocumentType.REPAIR_ESTIMATE, true)
                ),
                false,
                true,
                "Accident - late filing"
        );
        runTest("Auto claim filed too late (>30 days)", lateAuto);

        // Auto claim missing documents
        Claim missingDocsAuto = new Claim(
                "CLM-012",
                ClaimType.AUTO,
                3000.0,
                LocalDate.now().minusDays(10),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true)
                        // Missing: police report, photos, repair estimate
                ),
                false,
                true,
                "Hit and run - incomplete docs"
        );
        runTest("Auto claim with missing documents", missingDocsAuto);

        // ====================================================================
        // Fraud Detection
        // ====================================================================

        System.out.println("--- Fraud Detection ---\n");

        Claim fraudulent = new Claim(
                "CLM-020",
                ClaimType.PROPERTY,
                50000.0,
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                activeClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.PHOTOS, true),
                        new Document(DocumentType.PROOF_OF_OWNERSHIP, true),
                        new Document(DocumentType.REPAIR_ESTIMATE, true)
                ),
                true,  // Flagged for fraud!
                true,
                "Suspicious claim"
        );
        runTest("Claim flagged for fraud", fraudulent);

        // ====================================================================
        // Policy Status Issues
        // ====================================================================

        System.out.println("--- Policy Status Issues ---\n");

        Claim expiredPolicy = new Claim(
                "CLM-030",
                ClaimType.MEDICAL,
                1000.0,
                LocalDate.now().minusDays(10),
                LocalDate.now(),
                expiredClaimant,
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.MEDICAL_RECORDS, true)
                ),
                false,
                false,
                "Expired policy claim"
        );
        runTest("Claim on expired/inactive policy", expiredPolicy);

        // ====================================================================
        // Coverage Limits
        // ====================================================================

        System.out.println("--- Coverage Limits ---\n");

        // Claim exceeds STANDARD tier limit ($50,000)
        Claim exceedsCoverage = new Claim(
                "CLM-040",
                ClaimType.PROPERTY,
                75000.0,  // Exceeds STANDARD tier max of $50,000
                LocalDate.now().minusDays(10),
                LocalDate.now(),
                activeClaimant,  // STANDARD tier
                List.of(
                        new Document(DocumentType.CLAIM_FORM, true),
                        new Document(DocumentType.PHOTOS, true),
                        new Document(DocumentType.PROOF_OF_OWNERSHIP, true),
                        new Document(DocumentType.REPAIR_ESTIMATE, true)
                ),
                false,
                true,
                "Major property damage"
        );
        runTest("Claim exceeds coverage limit", exceedsCoverage);

        // ====================================================================
        // Evaluate All - Collect All Failures
        // ====================================================================

        System.out.println("--- Using evaluateAll (collects ALL failures) ---\n");

        // Create a claim with multiple issues
        Claim multipleIssues = new Claim(
                "CLM-099",
                ClaimType.AUTO,
                100000.0,  // Exceeds coverage
                LocalDate.of(2022, 1, 1),  // Before policy start
                LocalDate.now(),
                expiredClaimant,  // Expired policy
                List.of(
                        new Document(DocumentType.CLAIM_FORM, false)  // Unverified, missing others
                ),
                true,  // Flagged for fraud
                false,
                "Everything wrong"
        );

        Policy<Claim, ClaimDenialReason> policy = ClaimPolicies.getPolicyFor(multipleIssues);
        PolicyResult<ClaimDenialReason> allResults = policy.evaluateAll(multipleIssues);

        System.out.println("Claim with multiple issues:");
        System.out.println("  Claim ID: " + multipleIssues.claimId());
        System.out.println("  Type: " + multipleIssues.type());
        System.out.println("  Amount: $" + multipleIssues.amount());
        System.out.println();
        System.out.println("  All Passed: " + allResults.getAllPassed());
        System.out.println("  All Failure Reasons: " + allResults.failureReasons());
        System.out.println("  Failed Specifications:");
        allResults.failedResults().forEach(r ->
                System.out.println("    - " + r.getName() + ": " + r.getFailureReasons())
        );
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private static void runTest(String testName, Claim claim) {
        Policy<Claim, ClaimDenialReason> policy = ClaimPolicies.getPolicyFor(claim);
        PolicyResult<ClaimDenialReason> result = policy.evaluateFailFast(claim);

        System.out.println(testName + ":");
        System.out.println("  Claim ID: " + claim.claimId() + ", Type: " + claim.type() + ", Amount: $" + claim.amount());

        if (result.getAllPassed()) {
            System.out.println("  Result: ✅ APPROVED");
        } else {
            System.out.println("  Result: ❌ DENIED - " + result.failureReasons());
        }

        System.out.println();
    }
}

