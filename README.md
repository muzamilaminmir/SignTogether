# SignTogether - Breaking Barriers with AI 🤟

**SignTogether** is a state-of-the-art communication ecosystem designed to bridge the gap between the Deaf/Hard-of-Hearing community and the hearing world. By merging Advanced AI, Computer Vision, and 3D Avatar technology, we've created a seamless bridge for real-time two-way communication.

---

## 🚀 Key Features

### 1. 🤟 Sign → Text & Speech
-   **Real-Time Recognition**: Translates ISL signs in real-time using the device camera.
-   **TFLite & MediaPipe**: Powered by high-performance hand landmark detection and custom ML models.
-   **Text-to-Speech (TTS)**: Instantly vocalizes translated text for natural conversation.
-   **Sign of the Day**: A dedicated Home screen feature that teaches one new ISL sign daily.

### 2. 🗣️ Speech/Text → Sign
-   **3D Animated Avatar**: Fluid and accurate sign language rendering.
-   **Voice Recognition**: Converts spoken words into sign language animations instantly.

### 3. 👶 Kid Mode (Gamified Safety)
-   **Parental PIN Lock**: Robust security for children.
-   **Educational Quizzes**: 10+ real ISL questions per difficulty tier (Beginner, Intermediate, Advanced).
-   **Safe SOS**: Simplified one-tap help button for children to contact their guardians.
-   **Interactive UI**: Playful design with animated emojis and friendly feedback.

### 4. 🏥 Help Desk Mode (Institutional Solution)
-   **Pro Professional Interface**: Specialized UI for hospitals, banks, and government offices.
-   **Provider Panel**: Quick preset phrases for staff to communicate common instructions.
-   **User Camera**: Focused layout for deaf users to sign their requests.

### 5. 🚨 Advanced Emergency SOS
-   **GPS Location Integration**: Sends emergency SMS with a direct Google Maps link.
-   **Strobe Alert**: Activates the flashlight for visual signaling.
-   **Anti-Spam Guard**: 30-second cooldown timer to prevent accidental message floods.
-   **Announcement Audio**: Local audio siren/announcement during emergency activation.

### 6. 📚 Learning Center
-   **Curated Lessons**: Categorized video tutorials (Alphabet, Greetings, Emergency, Family).
-   **YouTube Integration**: Reliable playback with thumbnail previews.
-   **NGO Locator**: Discover nearby organizations supporting the deaf community.

---

## 🛠️ Tech Stack

-   **Frontend**: Jetpack Compose (Material 3), Vanilla CSS (Web)
-   **AI/ML**: MediaPipe (Hand Detection), TensorFlow Lite (ISL Classification)
-   **Storage**: Room DB (Conversation History), Jetpack DataStore (User Preferences/PIN)
-   **Services**: Google Maps Platform, Fused Location Provider, SMS Manager API
-   **Development**: Kotlin (Native Android), Python (AI Hub), Flask (Web Preview)

---

## 🎨 Design Philosophy
-   **Modern Aesthetics**: Glassmorphism, sleek dark mode, and vibrant gradient themes.
-   **Animation-First**: Smooth transitions with `fadeIn/fadeOut` and `slideIn/slideOut` logic for a premium feel.
-   **Accessibility-First**: High contrast ratios, large touch targets, and visual backup for audio alerts.

---

## 📦 Installation & Setup

### Native Android App
1.  Open the `android-project` folder in **Android Studio (Ladybug or later)**.
2.  Add your Google Maps API key in `local.properties`.
3.  Build and run on a physical device for best camera performance.

### Web Interface
1.  `cd web-app`
2.  `pip install -r requirements.txt`
3.  `python app.py`

---

## 👥 Meet the Team (Unity)

-   **Muzamil Mir** - Tech Lead & System Architect
-   **Simar Kaur** - Strategy & Design

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Empowering Inclusivity through Technology.*
