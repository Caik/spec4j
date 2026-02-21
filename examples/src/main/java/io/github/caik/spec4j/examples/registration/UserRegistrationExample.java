package io.github.caik.spec4j.examples.registration;

import io.github.caik.spec4j.Policy;
import io.github.caik.spec4j.PolicyResult;
import io.github.caik.spec4j.Specification;
import io.github.caik.spec4j.SpecificationFactory;
import io.github.caik.spec4j.SpecificationResult;

/**
 * User Registration Validation Example
 *
 * <p>Demonstrates basic usage of the Specification/Policy framework from Java:
 * <ul>
 *   <li>Creating specifications with {@code Specification.of()}</li>
 *   <li>Building policies with the fluent API</li>
 *   <li>Using {@code allOf()} for composite specifications</li>
 *   <li>Both evaluation modes: {@code evaluateFailFast()} and {@code evaluateAll()}</li>
 * </ul>
 */
public class UserRegistrationExample {

    // ========================================================================
    // Failure Reasons
    // ========================================================================

    public enum RegistrationFailure {
        USERNAME_TOO_SHORT,
        USERNAME_INVALID_CHARS,
        EMAIL_INVALID,
        PASSWORD_TOO_SHORT,
        PASSWORD_NO_UPPERCASE,
        PASSWORD_NO_NUMBER,
        AGE_TOO_YOUNG,
        TERMS_NOT_ACCEPTED
    }

    // ========================================================================
    // Context (User Registration Data)
    // ========================================================================

    public record RegistrationRequest(
            String username,
            String email,
            String password,
            int age,
            boolean termsAccepted
    ) {}

    // ========================================================================
    // Specifications
    // ========================================================================

    // Username must be at least 3 characters
    static final Specification<RegistrationRequest, RegistrationFailure> USERNAME_MIN_LENGTH =
            Specification.of(
                    "UsernameMinLength",
                    req -> req.username() != null && req.username().length() >= 3,
                    RegistrationFailure.USERNAME_TOO_SHORT
            );

    // Username must contain only alphanumeric characters and underscores
    static final Specification<RegistrationRequest, RegistrationFailure> USERNAME_VALID_CHARS =
            Specification.of(
                    "UsernameValidChars",
                    req -> req.username() != null && req.username().matches("^\\w+$"),
                    RegistrationFailure.USERNAME_INVALID_CHARS
            );

    // Email must be valid format
    static final Specification<RegistrationRequest, RegistrationFailure> EMAIL_VALID =
            Specification.of(
                    "EmailValid",
                    req -> req.email() != null && req.email().matches("^[^@]+@[^@]+\\.[^@]+$"),
                    RegistrationFailure.EMAIL_INVALID
            );

    // Password must be at least 8 characters
    static final Specification<RegistrationRequest, RegistrationFailure> PASSWORD_MIN_LENGTH =
            Specification.of(
                    "PasswordMinLength",
                    req -> req.password() != null && req.password().length() >= 8,
                    RegistrationFailure.PASSWORD_TOO_SHORT
            );

    // Password must contain at least one uppercase letter
    static final Specification<RegistrationRequest, RegistrationFailure> PASSWORD_HAS_UPPERCASE =
            Specification.of(
                    "PasswordHasUppercase",
                    req -> req.password() != null && req.password().matches(".*[A-Z].*"),
                    RegistrationFailure.PASSWORD_NO_UPPERCASE
            );

    // Password must contain at least one number
    static final Specification<RegistrationRequest, RegistrationFailure> PASSWORD_HAS_NUMBER =
            Specification.of(
                    "PasswordHasNumber",
                    req -> req.password() != null && req.password().matches(".*\\d.*"),
                    RegistrationFailure.PASSWORD_NO_NUMBER
            );

    // User must be at least 13 years old
    static final Specification<RegistrationRequest, RegistrationFailure> AGE_MINIMUM =
            Specification.of(
                    "AgeMinimum",
                    req -> req.age() >= 13,
                    RegistrationFailure.AGE_TOO_YOUNG
            );

    // User must accept terms and conditions
    static final Specification<RegistrationRequest, RegistrationFailure> TERMS_ACCEPTED =
            Specification.of(
                    "TermsAccepted",
                    RegistrationRequest::termsAccepted,
                    RegistrationFailure.TERMS_NOT_ACCEPTED
            );

    // ========================================================================
    // Composite Specifications
    // ========================================================================

    // All username requirements combined
    static final Specification<RegistrationRequest, RegistrationFailure> VALID_USERNAME =
            SpecificationFactory.allOf(
                    "ValidUsername",
                    USERNAME_MIN_LENGTH,
                    USERNAME_VALID_CHARS
            );

    // All password requirements combined
    static final Specification<RegistrationRequest, RegistrationFailure> VALID_PASSWORD =
            SpecificationFactory.allOf(
                    "ValidPassword",
                    PASSWORD_MIN_LENGTH,
                    PASSWORD_HAS_UPPERCASE,
                    PASSWORD_HAS_NUMBER
            );

    // ========================================================================
    // Policy
    // ========================================================================

    static final Policy<RegistrationRequest, RegistrationFailure> REGISTRATION_POLICY =
            Policy.<RegistrationRequest, RegistrationFailure>create()
                    .with(VALID_USERNAME)
                    .with(EMAIL_VALID)
                    .with(VALID_PASSWORD)
                    .with(AGE_MINIMUM)
                    .with(TERMS_ACCEPTED);

    // ========================================================================
    // Main
    // ========================================================================

    public static void main(String[] args) {
        System.out.println("=== User Registration Validation Example ===\n");

        // Valid registration
        var validRequest = new RegistrationRequest(
                "john_doe",
                "john@example.com",
                "SecurePass123",
                25,
                true
        );
        runTest("Valid registration", validRequest);

        // Invalid username (too short)
        var shortUsername = new RegistrationRequest(
                "jo",
                "john@example.com",
                "SecurePass123",
                25,
                true
        );
        runTest("Username too short", shortUsername);

        // Invalid username (special characters)
        var invalidChars = new RegistrationRequest(
                "john@doe!",
                "john@example.com",
                "SecurePass123",
                25,
                true
        );
        runTest("Username with invalid characters", invalidChars);

        // Weak password (no uppercase)
        var weakPassword = new RegistrationRequest(
                "john_doe",
                "john@example.com",
                "weakpass123",
                25,
                true
        );
        runTest("Password without uppercase", weakPassword);

        // Too young
        var tooYoung = new RegistrationRequest(
                "young_user",
                "young@example.com",
                "SecurePass123",
                10,
                true
        );
        runTest("User too young (10)", tooYoung);

        // Terms not accepted
        var noTerms = new RegistrationRequest(
                "john_doe",
                "john@example.com",
                "SecurePass123",
                25,
                false
        );
        runTest("Terms not accepted", noTerms);

        // Using evaluateAll to collect ALL failures
        System.out.println("--- Using evaluateAll (collects all failures) ---\n");

        var multipleIssues = new RegistrationRequest(
                "x",              // too short
                "invalid-email",  // invalid format
                "weak",           // too short, no uppercase, no number
                10,               // too young
                false             // terms not accepted
        );

        PolicyResult<RegistrationFailure> allResults = REGISTRATION_POLICY.evaluateAll(multipleIssues);

        System.out.println("Multiple issues:");
        System.out.println("  Request: " + multipleIssues);
        System.out.println("  All failure reasons: " + allResults.failureReasons());
        System.out.println("  Failed specifications: " +
                allResults.failedResults().stream()
                        .map(SpecificationResult::getName)
                        .toList());
    }

    private static void runTest(String testName, RegistrationRequest request) {
        PolicyResult<RegistrationFailure> result = REGISTRATION_POLICY.evaluateFailFast(request);

        System.out.println(testName + ":");
        System.out.println("  Request: " + request);

        if (result.getAllPassed()) {
            System.out.println("  Result: ✅ REGISTRATION ALLOWED");
        } else {
            System.out.println("  Result: ❌ REGISTRATION DENIED - " + result.failureReasons());
        }

        System.out.println();
    }
}

