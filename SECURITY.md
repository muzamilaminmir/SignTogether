# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| v6.x    | ✅ Active Support  |
| v5.x    | ⚠️ Security Only   |
| < v5.0  | ❌ End of Life     |

## Reporting a Vulnerability

We take the security of SignTogether seriously. If you discover a security vulnerability, please follow these steps:

### 1. 📧 Contact Us Privately

**Do NOT open a public issue for security vulnerabilities.**

Instead, please email us at: **mirmuzamil962@gmail.com**

### 2. 📝 Include the Following Details

- **Description** of the vulnerability
- **Steps to reproduce** the issue
- **Potential impact** (e.g., data exposure, unauthorized access)
- **Suggested fix** (if you have one)

### 3. ⏱️ Response Timeline

| Action | Timeframe |
|--------|-----------|
| Acknowledgment | Within 48 hours |
| Initial Assessment | Within 5 business days |
| Fix & Patch | Depends on severity (Critical: 7 days, High: 14 days, Medium: 30 days) |

### 4. 🏆 Recognition

We appreciate responsible disclosure. Contributors who report valid vulnerabilities will be:
- Credited in our release notes (with your permission)
- Added to our Security Hall of Fame

## Security Considerations

### On-Device Processing
- All ML inference (TensorFlow Lite, MediaPipe) runs **locally on-device**
- No sign language video data is transmitted to external servers
- Camera feed is processed in-memory and never persisted

### Data Storage
- Conversation history is stored locally using **Room Database**
- User preferences (including Kid Mode PIN) are stored via **Jetpack DataStore**
- No user data is collected or sent to analytics services

### Network Communication
- The Flask web server uses **SSL/TLS** encryption
- API endpoints validate input to prevent injection attacks
- Emergency SOS uses the device's native SMS capability (no third-party services)

### Permissions
The app requests only the minimum permissions required:
- `CAMERA` — For sign language recognition
- `ACCESS_FINE_LOCATION` — For Emergency SOS GPS coordinates
- `SEND_SMS` — For Emergency SOS alerts
- `INTERNET` — For web interface and content loading
- `FLASHLIGHT` — For emergency strobe alert

## Best Practices for Users
- Keep the app updated to the latest version
- Set a strong PIN for Kid Mode
- Verify emergency contacts are correct before relying on SOS
- Do not share your device with untrusted individuals when in Help Desk Mode
