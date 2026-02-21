package io.github.caik.spec4j.examples.insurance.model;

/**
 * Represents a document submitted with an insurance claim.
 */
public record Document(
        DocumentType type,
        boolean verified
) {}

