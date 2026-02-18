# 🤝 Contributing to Spec4j

Thank you for your interest in contributing to Spec4j! 🎉 This document provides guidelines and instructions for contributing.

## 🚀 Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/spec4j.git
   cd spec4j
   ```
3. **Build** the project:
   ```bash
   ./gradlew build
   ```

## 💻 Development Workflow

### 🌿 Branching

- Create a feature branch from `main`:
  ```bash
  git checkout -b feature/your-feature-name
  ```
- Use descriptive branch names: `feature/add-dsl`, `fix/null-handling`, `docs/improve-readme`

### ✏️ Making Changes

1. Write your code following the existing style
2. Add tests for new functionality
3. Ensure all tests pass:
   ```bash
   ./gradlew test
   ```
4. Check code coverage (aim for >90%):
   ```bash
   ./gradlew jacocoTestReport
   # Open lib/build/reports/jacoco/test/html/index.html
   ```

### 💬 Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/) for clear, structured commit history.

**Format:**
```
<type>: <description>

[optional body]
```

**Types:**

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `test` | Adding or updating tests |
| `refactor` | Code change that neither fixes nor adds features |
| `chore` | Maintenance (build, CI, dependencies) |

**Examples:**
```
feat: add not() composite specification
fix: handle null values in Specification.of()
docs: update installation instructions in README
test: add unit tests for CompositeSpecification
refactor: simplify Policy evaluation logic
```

### 🔀 Pull Requests

1. Push your branch to your fork
2. Open a Pull Request against `main`
3. Fill out the PR description:
   - What does this PR do?
   - Why is this change needed?
   - How was it tested?
4. Wait for review and address any feedback

## 🎨 Code Style

- Follow Kotlin coding conventions
- Use meaningful names for classes, functions, and variables
- Keep functions small and focused
- Document public APIs with KDoc comments

## 🧪 Testing

- Write unit tests for all new functionality
- Place tests in `lib/src/test/kotlin/` mirroring the main source structure
- Use descriptive test names:
  ```kotlin
  @Test
  fun `anyOf passes when at least one specification passes`() { ... }
  ```

## 🐛 Reporting Issues

When reporting bugs, please include:

1. A clear description of the issue
2. Steps to reproduce
3. Expected vs actual behavior
4. Kotlin/JDK version
5. Minimal code example if applicable

## 💡 Feature Requests

Feature requests are welcome! Please:

1. Check existing issues first to avoid duplicates
2. Describe the use case and motivation
3. Provide examples of how it would be used

## ❓ Questions?

Feel free to open an issue for questions or discussions.

---

Thank you for contributing! 🎉

