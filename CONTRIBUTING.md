# Contributing to SignTogether 🤟

Thank you for your interest in contributing to SignTogether! This project aims to break communication barriers for the Deaf/Hard-of-Hearing community, and every contribution makes a difference.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

---

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold respectful and inclusive communication. We prioritize accessibility and inclusion in all aspects of this project.

---

## Getting Started

### Prerequisites

- **Android Development**: Android Studio Ladybug or later, JDK 17+
- **Web Development**: Python 3.8+, pip
- **ML/AI**: Basic understanding of TensorFlow, MediaPipe

### Repository Structure

- `android-project/` — Native Kotlin/Jetpack Compose Android app
- `app.py` — Flask web server
- `ai_engine.py` — Python AI/ML processing engine
- `templates/` — Web HTML templates
- `static/` — Web static assets

---

## Development Setup

### Android App

```bash
# Clone the repository
git clone https://github.com/muzamilaminmir/SignTogether.git
cd SignTogether

# Open android-project/ in Android Studio
# Sync Gradle and build
```

### Web Server

```bash
# Install dependencies
pip install -r requirements.txt

# Run the development server
python app.py
```

---

## How to Contribute

### 🐛 Bug Reports

1. Check existing [Issues](https://github.com/muzamilaminmir/SignTogether/issues) first
2. Use the Bug Report template
3. Include:
   - Device model and Android version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots or screen recordings (if applicable)

### ✨ Feature Requests

1. Open an [Issue](https://github.com/muzamilaminmir/SignTogether/issues/new) with the `enhancement` label
2. Describe the feature and its impact on accessibility
3. Explain how it fits within the existing architecture

### 🔧 Code Contributions

1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes
4. Write or update tests
5. Submit a Pull Request

---

## Coding Standards

### Kotlin (Android)

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Jetpack Compose for all new UI screens
- Follow MVVM architecture with ViewModels
- Use string resources (`strings.xml`) for all user-facing text
- Support dark mode in all new components

### Python (Backend)

- Follow PEP 8 style guidelines
- Use type hints where applicable
- Add docstrings to all public functions and classes

### General

- Keep functions focused and small
- Add comments for complex logic
- Ensure accessibility in all UI components

---

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new ISL gesture for "thank you"
fix: correct hand landmark detection for left-handed users
docs: update README with new architecture diagram
style: format Kotlin files with ktlint
refactor: extract sign detection logic into separate module
test: add unit tests for ConversationViewModel
chore: update Gradle dependencies
```

---

## Pull Request Process

1. **Title**: Use a clear, descriptive title following commit conventions
2. **Description**: Explain what changes were made and why
3. **Testing**: Describe how you tested your changes
4. **Screenshots**: Include before/after screenshots for UI changes
5. **Review**: At least one maintainer must approve before merging

### PR Checklist

- [ ] Code follows the project's coding standards
- [ ] Self-reviewed the code for obvious errors
- [ ] Added/updated relevant documentation
- [ ] Tested on a physical Android device (for app changes)
- [ ] No new warnings introduced
- [ ] String resources used (no hardcoded strings in UI)

---

## 💡 Areas We Need Help With

- 🌍 **Translations** — Adding more language support (values-XX string files)
- 🤟 **ISL Gesture Data** — Expanding the sign language vocabulary
- ♿ **Accessibility** — Improving screen reader support and touch targets
- 🧪 **Testing** — Unit tests for ViewModels and integration tests
- 📱 **UI/UX** — Polishing animations and user flows
- 📝 **Documentation** — API docs, user guides, and tutorials

---

## 📧 Contact

For questions or discussions about contributing:
- **Email**: mirmuzamil962@gmail.com
- **GitHub Issues**: [Open an Issue](https://github.com/muzamilaminmir/SignTogether/issues)

---

*Thank you for helping make communication accessible for everyone! 🤟*
