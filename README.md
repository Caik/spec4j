# 📋 Spec4j

A Kotlin implementation of the [Specification Pattern](https://en.wikipedia.org/wiki/Specification_pattern) for composable, reusable business rules.

**Stop scattering validation logic across your codebase.** Spec4j lets you define small, testable business rules as *specifications* and combine them into *policies* — making your domain logic explicit, reusable, and easy to reason about.

[![CI](https://github.com/caik/spec4j/actions/workflows/ci.yml/badge.svg)](https://github.com/caik/spec4j/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/caik/spec4j/graph/badge.svg)](https://codecov.io/gh/caik/spec4j)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.caik/spec4j-core)](https://central.sonatype.com/artifact/io.github.caik/spec4j-core)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-17+-orange.svg)](https://openjdk.org/)

## 📑 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Core Concepts](#-core-concepts)
- [Examples](#-examples)
- [Building](#-building)
- [Contributing](#-contributing)
- [License](#%EF%B8%8F-license)

## 🔍 Overview

Spec4j provides a clean, type-safe way to define and evaluate business rules. Instead of scattering validation logic throughout your codebase, you define atomic **specifications** that can be composed into **policies**.

```kotlin
// Define specifications
val isAdult = Specification.of<User, Reason>("IsAdult", { it.age >= 18 }, Reason.UNDERAGE)
val hasVerifiedEmail = Specification.of<User, Reason>("HasVerifiedEmail", { it.emailVerified }, Reason.EMAIL_NOT_VERIFIED)

// Compose into a policy
val registrationPolicy = Policy.create<User, Reason>()
    .with(isAdult)
    .with(hasVerifiedEmail)

// Evaluate
val result = registrationPolicy.evaluateFailFast(user)
if (result.allPassed) {
    // proceed
} else {
    // handle result.failureReasons()
}
```

## ✨ Features

- 🧩 **Composable** — Build complex rules from simple, reusable specifications
- 🔒 **Type-safe** — Failure reasons are enums, not strings
- ⚡ **Two evaluation modes** — `evaluateFailFast` (stops on first failure) or `evaluateAll` (collects all failures)
- 🔗 **Logical operators** — `allOf`, `anyOf`, `not` for combining specifications
- 💜 **Kotlin-first** — Leverages `fun interface` for clean lambda syntax

## 📦 Installation

> 📌 Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.caik/spec4j-core).

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.caik:spec4j-core:VERSION")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.caik:spec4j-core:VERSION'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.caik</groupId>
    <artifactId>spec4j-core</artifactId>
    <version>VERSION</version>
</dependency>
```

## 📚 Core Concepts

### 📌 Specification

A single, atomic condition that evaluates a context and returns pass/fail with a reason:

```kotlin
val minimumAge = Specification.of<LoanApplication, Reason>(
    "MinimumAge",
    { it.applicantAge >= 18 },
    Reason.APPLICANT_TOO_YOUNG
)
```

### 📜 Policy

A named collection of specifications that together define a business rule:

```kotlin
val loanEligibility = Policy.create<LoanApplication, Reason>()
    .with(minimumAge)
    .with(maximumAge)
    .with(creditCheck)
```

### 🔗 Composites

Combine specifications with logical operators:

```kotlin
// AND — all must pass
val fullyVerified = SpecificationFactory.allOf("FullyVerified", emailVerified, phoneVerified)

// OR — at least one must pass  
val hasPaymentMethod = SpecificationFactory.anyOf("HasPayment", hasCreditCard, hasBankAccount)

// NOT — inverts the result
val notBlocked = SpecificationFactory.not("NotBlocked", Reason.BLOCKED, isBlockedCountry)
```

## 🎯 Examples

The `examples` module contains complete working examples showcasing Spec4j's capabilities:

```bash
# Loan eligibility (basic usage)
./gradlew :examples:run -PexampleName=LoanEligibility

# E-commerce order validation (custom specs, not(), reusable specs)
./gradlew :examples:run -PexampleName=OrderValidation

# Feature access control (multiple policies, dynamic specs)
./gradlew :examples:run -PexampleName=FeatureAccess
```

## 🛠️ Building

```bash
# Build and run tests
./gradlew build

# Run tests with coverage report
./gradlew test jacocoTestReport
# Report: lib/build/reports/jacoco/test/html/index.html
```

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Caik/spec4j/blob/main/CONTRIBUTING.md) for guidelines.

## ⚖️ License

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Caik/spec4j/blob/main/LICENSE)

Released 2026 by [Carlos Henrique Severino](https://github.com/Caik)

