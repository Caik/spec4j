package io.github.caik.spec4j.examples.insurance.spec;

import io.github.caik.spec4j.Specification;
import io.github.caik.spec4j.SpecificationResult;
import io.github.caik.spec4j.examples.insurance.model.Claim;
import io.github.caik.spec4j.examples.insurance.model.ClaimType;
import io.github.caik.spec4j.examples.insurance.model.Document;
import io.github.caik.spec4j.examples.insurance.model.DocumentType;
import io.github.caik.spec4j.examples.insurance.reason.ClaimDenialReason;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Custom Specification implementation for document compliance validation.
 * 
 * <p>This demonstrates implementing the {@link Specification} interface directly
 * (rather than using {@code Specification.of()}) for complex validation logic
 * that may return multiple failure reasons.
 * 
 * <p>Different claim types require different documents:
 * <ul>
 *   <li>MEDICAL: Claim form, medical records</li>
 *   <li>AUTO: Claim form, police report, photos, repair estimate</li>
 *   <li>PROPERTY: Claim form, photos, proof of ownership, repair estimate</li>
 *   <li>LIFE: Claim form, death certificate</li>
 * </ul>
 */
public class DocumentComplianceSpec implements Specification<Claim, ClaimDenialReason> {

    @NotNull
    @Override
    public String name() {
        return "DocumentCompliance";
    }

    @NotNull
    @Override
    public SpecificationResult<ClaimDenialReason> evaluate(Claim claim) {
        Set<DocumentType> requiredDocs = getRequiredDocuments(claim.type());
        Set<DocumentType> submittedDocs = claim.documents().stream()
                .map(Document::type)
                .collect(Collectors.toSet());

        List<ClaimDenialReason> failures = new ArrayList<>();

        // Check for missing documents
        Set<DocumentType> missingDocs = EnumSet.copyOf(requiredDocs);
        missingDocs.removeAll(submittedDocs);

        if (!missingDocs.isEmpty()) {
            // Add one MISSING_REQUIRED_DOCUMENTS reason per missing document
            for (int i = 0; i < missingDocs.size(); i++) {
                failures.add(ClaimDenialReason.MISSING_REQUIRED_DOCUMENTS);
            }
        }

        // Check that all submitted documents are verified
        boolean hasUnverifiedDocs = claim.documents().stream()
                .filter(doc -> requiredDocs.contains(doc.type()))
                .anyMatch(doc -> !doc.verified());

        if (hasUnverifiedDocs) {
            failures.add(ClaimDenialReason.DOCUMENTS_NOT_VERIFIED);
        }

        if (failures.isEmpty()) {
            return SpecificationResult.pass(name());
        } else {
            return SpecificationResult.fail(name(), failures);
        }
    }

    /**
     * Returns the set of required documents for a given claim type.
     */
    private Set<DocumentType> getRequiredDocuments(ClaimType claimType) {
        return switch (claimType) {
            case MEDICAL -> EnumSet.of(
                    DocumentType.CLAIM_FORM,
                    DocumentType.MEDICAL_RECORDS
            );
            case AUTO -> EnumSet.of(
                    DocumentType.CLAIM_FORM,
                    DocumentType.POLICE_REPORT,
                    DocumentType.PHOTOS,
                    DocumentType.REPAIR_ESTIMATE
            );
            case PROPERTY -> EnumSet.of(
                    DocumentType.CLAIM_FORM,
                    DocumentType.PHOTOS,
                    DocumentType.PROOF_OF_OWNERSHIP,
                    DocumentType.REPAIR_ESTIMATE
            );
            case LIFE -> EnumSet.of(
                    DocumentType.CLAIM_FORM,
                    DocumentType.DEATH_CERTIFICATE
            );
        };
    }
}

