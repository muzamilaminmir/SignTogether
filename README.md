<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/ML-TensorFlow%20Lite-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white" alt="TensorFlow Lite"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="MIT License"/>
</p>

<h1 align="center">🤟 SignTogether</h1>
<h3 align="center"><em>Breaking Barriers with AI — Real-Time Two-Way Sign Language Communication</em></h3>

<p align="center">
  <strong>SignTogether</strong> is a state-of-the-art communication ecosystem designed to bridge the gap between the Deaf/Hard-of-Hearing community and the hearing world. By merging Advanced AI, Computer Vision, and 3D Avatar technology, we've created a seamless bridge for real-time two-way communication.
</p>

---

## 📋 Table of Contents

- [✨ Key Features](#-key-features)
- [🏗️ Architecture](#️-architecture)
- [🛠️ Tech Stack](#️-tech-stack)
- [🎨 Design Philosophy](#-design-philosophy)
- [📦 Installation & Setup](#-installation--setup)
- [📁 Project Structure](#-project-structure)
- [📱 App Modes](#-app-modes)
- [🤝 Contributing](#-contributing)
- [👥 Meet the Team](#-meet-the-team)
- [📄 License](#-license)

---

## ✨ Key Features

### 1. 🤟 Sign → Text & Speech
- **Real-Time Recognition** — Translates ISL signs in real-time using the device camera
- **TFLite & MediaPipe** — Powered by high-performance hand landmark detection and custom ML models
- **Text-to-Speech (TTS)** — Instantly vocalizes translated text for natural conversation
- **Sign of the Day** — A dedicated Home screen feature that teaches one new ISL sign daily
- **Conversation History** — Full session logging with Room Database for review & export

### 2. 🗣️ Speech/Text → Sign
- **3D Animated Avatar** — Fluid and accurate sign language rendering via SceneView
- **Voice Recognition** — Converts spoken words into sign language animations instantly
- **Comprehensive Video Dictionary** — 80+ word-level ISL sign videos with alphabetic fallback

### 3. 👶 Kid Mode (Gamified Safety)
- **Parental PIN Lock** — Robust security for children via Jetpack DataStore
- **Educational Quizzes** — 10+ real ISL questions per difficulty tier (Beginner, Intermediate, Advanced)
- **Safe SOS** — Simplified one-tap help button for children to contact their guardians
- **Interactive UI** — Playful design with animated emojis and friendly feedback

### 4. 🏥 Help Desk Mode (Institutional Solution)
- **Professional Interface** — Specialized UI for hospitals, banks, and government offices
- **Provider Panel** — Quick preset phrases for staff to communicate common instructions
- **User Camera** — Focused layout for deaf users to sign their requests
- **Session-based Logging** — Separate activity tracking with PDF export capability

### 5. 🚨 Advanced Emergency SOS
- **GPS Location Integration** — Sends emergency SMS with a direct Google Maps link
- **Strobe Alert** — Activates the flashlight for visual signaling
- **Anti-Spam Guard** — 30-second cooldown timer to prevent accidental message floods
- **Announcement Audio** — Local audio siren/announcement during emergency activation
- **Emergency Contacts Manager** — Add, edit, and manage trusted contacts

### 6. 📚 Learning Center
- **Curated Lessons** — Categorized video tutorials (Alphabet, Greetings, Emergency, Family)
- **YouTube Integration** — Reliable playback with thumbnail previews
- **NGO Locator** — Discover nearby organizations supporting the deaf community
- **Phrase Library** — Common ISL phrases for quick reference

### 7. 📊 Analytics & Insights
- **Usage Dashboard** — Track translation sessions, most-used signs, and learning progress
- **Conversation Export** — Export full chat sessions as PDF
- **Nearby Community** — Connect with other ISL users in your area

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│           Jetpack Compose UI Layer              │
│      (Material 3 + Dark Mode + Animations)      │
├─────────────────────────────────────────────────┤
│        Navigation + ViewModels (MVVM)           │
│   (NavGraph, Animated Transitions, State Mgmt)  │
├───────────┬───────────┬───────────┬─────────────┤
│  CameraX  │ MediaPipe │  TF Lite  │  SceneView  │
│ (Camera)  │ (Hands)   │   (ML)    │  (3D Avatar)│
├───────────┴───────────┴───────────┴─────────────┤
│      Room Database  +  Jetpack DataStore        │
│   (Conversations, History, User Prefs, PIN)     │
├─────────────────────────────────────────────────┤
│     Google Maps + Fused Location + SMS API      │
│        (SOS GPS + NGO Locator + Alerts)         │
├─────────────────────────────────────────────────┤
│          Flask Backend (Web Preview)            │
│    (Python AI Engine + WebSocket + REST API)    │
└─────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|:------|:-----------|
| **Language** | Kotlin (Native Android), Python (AI Hub) |
| **UI Framework** | Jetpack Compose + Material 3 |
| **AI/ML** | TensorFlow Lite (ISL Classification), MediaPipe (Hand Detection) |
| **Camera** | CameraX |
| **3D Rendering** | SceneView (Sign Language Avatar) |
| **Database** | Room (Conversation History & Sessions) |
| **Preferences** | Jetpack DataStore (User Settings, Kid Mode PIN) |
| **Location** | Google Maps Platform + Fused Location Provider |
| **Speech** | Android TTS + Speech Recognition |
| **Backend** | Flask, OpenCV, NumPy |
| **Architecture** | MVVM + Repository Pattern |
| **Min SDK** | Android 7.0 (API 24) |

---

## 🎨 Design Philosophy

- 🎭 **Modern Aesthetics** — Glassmorphism, sleek dark mode, and vibrant gradient themes
- ✨ **Animation-First** — Smooth transitions with `fadeIn/fadeOut` and `slideIn/slideOut` for a premium feel
- ♿ **Accessibility-First** — High contrast ratios, large touch targets, visual backup for audio alerts
- 🌐 **Internationalization** — Multi-language support (English + Hindi with `values-hi` strings)

---

## 📦 Installation & Setup

### 📱 Native Android App (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/muzamilaminmir/SignTogether.git
   cd SignTogether
   ```

2. Open the `android-project` folder in **Android Studio Ladybug** or later.

3. Add your Google Maps API key in `local.properties`:
   ```properties
   MAPS_API_KEY=your_api_key_here
   ```

4. Build and run on a **physical device** for best camera performance.

> **Note:** The TFLite model and MediaPipe run on-device — no internet required for core translation.

### 🌐 Web Interface (Development & Demo)

1. Navigate to the project root:
   ```bash
   cd SignTogether
   ```

2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Run the Flask server:
   ```bash
   python app.py
   ```

4. Open your browser at `https://localhost:5000`

> **Note:** Web interface requires a webcam. The server uses SSL with self-signed certificates.

### 📲 Pre-Built APK

Download the latest APK from the repository root:
- `SignTogether_v6_Fixed.apk` — Latest stable build with all features

---

## 📁 Project Structure

```
SignTogether/
├── android-project/                    # Native Android App (Kotlin)
│   └── app/src/main/
│       ├── java/com/signtogether/
│       │   ├── MainActivity.kt         # App entry point
│       │   ├── SignTogetherApp.kt       # Application class
│       │   ├── SignLanguageProcessor.java  # ML inference engine
│       │   ├── VirtualRobot.java        # Avatar state manager
│       │   ├── data/
│       │   │   ├── room/               # Room DB (conversations, sessions)
│       │   │   ├── StoreUserProfile.kt  # DataStore preferences
│       │   │   └── SubscriptionManager.kt
│       │   ├── model/
│       │   │   └── LanguageModelManager.kt  # TFLite model handler
│       │   ├── navigation/
│       │   │   ├── NavGraph.kt          # App navigation
│       │   │   └── Screen.kt           # Screen definitions
│       │   ├── ui/
│       │   │   ├── screens/            # All Compose screens
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── SignToTextScreen.kt
│       │   │   │   ├── TextToSignScreen.kt
│       │   │   │   ├── SOSScreen.kt
│       │   │   │   ├── KidQuizScreen.kt
│       │   │   │   ├── LearnScreen.kt
│       │   │   │   ├── NgoLocatorScreen.kt
│       │   │   │   └── ... (15+ screens)
│       │   │   └── theme/              # Material 3 theming
│       │   └── utils/
│       │       └── PdfExporter.kt      # Conversation PDF export
│       └── assets/
│           ├── pose_data/              # ISL gesture JSON data
│           └── signs/                  # Sign video assets
│
├── app.py                              # Flask web server
├── ai_engine.py                        # Python AI/ML engine
├── virtual_robot.py                    # Web virtual assistant
├── templates/                          # Web HTML templates
│   ├── index.html                      # Landing page
│   ├── model1.html                     # Text → Sign (video playback)
│   ├── model2.html                     # Sign → Text (camera)
│   ├── emergency.html                  # Emergency mode
│   ├── learn.html                      # Learning resources
│   └── ...
├── static/                             # Web assets (CSS, JS, images, sign videos)
├── model.h5                            # Trained CNN model
├── sign_model.h5                       # ISL classification model
├── CNN-Train-Model.py                  # Model training script
├── train_cnn.py                        # CNN training pipeline
├── requirements.txt                    # Python dependencies
└── README.md
```

---

## 📱 App Modes

| Mode | Target Audience | Key Features |
|:-----|:----------------|:-------------|
| **👤 Standard** | General users | Full translation, SOS, history, learning, analytics |
| **👶 Kid Mode** | Children (5-15) | Gamified quizzes, parental PIN lock, simplified SOS |
| **🏥 Help Desk** | Hospitals, banks, govt offices | Professional UI, preset phrases, session logging |

---

## 🌟 UN SDG Alignment

| SDG | Goal | How SignTogether Contributes |
|:----|:-----|:----------------------------|
| 🎯 SDG 3 | Good Health | Emergency SOS for deaf individuals in medical settings |
| 🎯 SDG 4 | Quality Education | ISL learning tools, quizzes, and daily sign education |
| 🎯 SDG 10 | Reduced Inequalities | Breaking communication barriers for 63M+ deaf Indians |
| 🎯 SDG 11 | Sustainable Cities | Inclusive public services via Help Desk Mode |

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on how to get started.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 👥 Meet the Team (Unity)

| Name | Role | Responsibilities |
|:-----|:-----|:-----------------|
| **Muzamil Mir** | Tech Lead & System Architect | System Architecture, AI Models, Full-Stack Development |
| **Simar Kaur** | Strategy & Design Lead | Coordination, Problem Definition, UI/UX, Presentation |

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <strong>🤟 Empowering Inclusivity Through Technology 🤟</strong>
  <br/>
  <em>"Every conversation matters. Let's make them all possible."</em>
  <br/><br/>
  Made with ❤️ by <strong>Team Unity</strong>
</p>
