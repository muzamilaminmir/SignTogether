# SignTogether — Presentation Slides

---

## SLIDE 1: Title Slide

**SignTogether**
*Bridging the Communication Gap Between Deaf & Hearing Worlds*

- Team:Unity
- Version 1.0 | Android App
- Built with: Kotlin, Jetpack Compose, TensorFlow Lite, MediaPipe

---

## SLIDE 2: The Problem

**422 Million+ people worldwide have disabling hearing loss** (WHO)

- 🚫 63 million deaf individuals in India alone
- 🚫 Only ~300 certified ISL interpreters for the entire country
- 🚫 Deaf individuals face barriers in hospitals, banks, schools & emergencies
- 🚫 Families struggle to communicate with deaf children
- 🚫 No affordable, real-time sign language translation tools exist

> *"Communication is a basic human right — not a privilege."*

---

## SLIDE 3: Our Solution

**SignTogether** — An AI-powered mobile app that enables real-time, two-way communication between deaf and hearing individuals.

| Feature | What It Does |
|---------|-------------|
| 🤟 Sign → Text/Speech | Camera detects ISL signs → converts to text & voice |
| 🗣️ Speech/Text → Sign | Speak or type → 3D avatar signs it back |
| 🆘 Emergency SOS | One-tap alert to contacts with GPS location |
| 📚 Learn ISL | Curated video lessons + daily sign of the day |
| 🏥 Help Desk Mode | Institutional tool for hospitals, banks, govt offices |
| 👶 Kid Mode | Safe, gamified learning environment for children |

---

## SLIDE 4: App Architecture

```
┌─────────────────────────────────┐
│          Jetpack Compose UI     │
│   (Material 3 + Dark Mode)     │
├─────────────────────────────────┤
│     Navigation + ViewModels     │
│   (Animated Transitions)       │
├──────────┬──────────┬───────────┤
│ CameraX  │ MediaPipe│ TF Lite   │
│ (Camera) │ (Hands)  │ (ML)      │
├──────────┴──────────┴───────────┤
│   Room Database + DataStore     │
│   (Offline Storage + Prefs)     │
├─────────────────────────────────┤
│   Google Maps + Location API    │
│   (SOS GPS + NGO Locator)       │
└─────────────────────────────────┘
```

---

## SLIDE 5: Key Feature — Sign to Text/Speech

**Real-Time Sign Language Recognition**

- 📸 CameraX captures live video feed
- 🖐️ MediaPipe detects 21 hand landmarks
- 🧠 TensorFlow Lite classifies ISL gestures
- 📝 Recognized text displayed on screen
- 🔊 Text-to-Speech reads it aloud

*Works completely offline — no internet required!*

---

## SLIDE 6: Key Feature — Text/Speech to Sign

**Reverse Communication Channel**

- 🎙️ Speech Recognition converts voice to text
- ✍️ User can also type directly
- 🧍 3D Avatar renders ISL signs in real-time
- 🔄 Two-way communication bridge

*Enables hearing people to "speak" in sign language*

---

## SLIDE 7: Key Feature — Emergency SOS

**One-Tap Emergency Alert System**

- 🔴 Large, accessible SOS button
- 📍 Auto-attaches GPS location (Google Maps link)
- 📱 Sends SMS to all saved emergency contacts
- 🔦 Activates flashlight strobe for visual alert
- 📢 Plays emergency audio announcement
- ⏱️ 30-second cooldown prevents accidental spam
- 👶 Kid Mode: simplified "I NEED HELP" with guardian-only alert

---

## SLIDE 8: Three Modes

### 👤 Standard Mode
Full-featured individual use — translate, learn, SOS, history

### 👶 Kid Mode (Ages 5-15)
- Gamified UI with friendly colors
- Sign Language Quiz (10 real ISL questions per tier)
- Parental PIN Lock — kids can't leave safe mode
- Simplified SOS (guardian-only)
- Class-based difficulty (Class 1-10)

### 🏥 Help Desk Mode (Institutional)
- Professional dark theme
- Session-based communication
- Preset quick phrases
- Dual tabs: Provider Panel + User Camera
- Separate activity logging

---

## SLIDE 9: Learning & Education

**Daily Sign of the Day**
- 30 ISL signs rotating daily on Home screen
- Sign name, how-to description, hand shape guide

**Video Tutorials**
- 4 curated ASL lesson categories
- Thumbnail previews with YouTube integration

**Interactive Quiz**
- 10 questions per difficulty tier (Beginner/Intermediate/Advanced)
- Real ISL knowledge — shuffled, varied answer positions
- Star ratings + shareable score cards

---

## SLIDE 10: Additional Features

| Feature | Description |
|---------|-------------|
| 🌙 Dark Mode | Fully functional theme toggle (persisted) |
| 📊 Analytics Dashboard | Usage statistics and insights |
| 📖 Phrase Library | Common ISL phrases for quick reference |
| 🗺️ NGO Locator | Find nearby deaf support organizations |
| 👥 Nearby Community | Connect with other ISL users |
| 📜 Translation History | Full conversation logs with export |
| ✨ Animated Transitions | Premium fade + slide navigation |
| 🎨 Onboarding | Beautiful guided intro with illustrations |

---

## SLIDE 11: Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **ML/AI** | TensorFlow Lite, MediaPipe |
| **Camera** | CameraX |
| **3D Rendering** | SceneView (Avatar) |
| **Database** | Room (SQLite) |
| **Preferences** | DataStore |
| **Location** | Google Maps + Fused Location |
| **Speech** | Android TTS + Speech Recognition |
| **Architecture** | MVVM + Repository Pattern |
| **Min SDK** | Android 7.0 (API 24) |

---

## SLIDE 12: Social Impact

### UN Sustainable Development Goals Alignment

- 🎯 **SDG 3** — Good Health: Emergency SOS for deaf individuals
- 🎯 **SDG 4** — Quality Education: ISL learning tools for all ages
- 🎯 **SDG 10** — Reduced Inequalities: Breaking communication barriers
- 🎯 **SDG 11** — Sustainable Cities: Inclusive public services via Help Desk

### Impact Numbers (Potential)
- 63M+ deaf individuals in India who could benefit
- 1.2M+ schools where Kid Mode can be deployed
- Thousands of hospitals, banks, courts needing Help Desk mode

---

## SLIDE 13: Competitive Advantage

| Feature | SignTogether | Others |
|---------|:-----------:|:------:|
| Real-time ISL detection | ✅ | ❌ (Most support ASL only) |
| Two-way communication | ✅ | ❌ |
| Offline functionality | ✅ | ❌ |
| Kid-safe learning mode | ✅ | ❌ |
| Institutional Help Desk | ✅ | ❌ |
| Emergency SOS with GPS | ✅ | ❌ |
| Free & open platform | ✅ | ❌ |

---

## SLIDE 14: Revenue Model (Future Scope)

| Tier | Price | Features |
|------|-------|----------|
| **Free** | ₹0 | Basic translation, SOS, 3 quiz levels |
| **Premium** | ₹149/mo | Unlimited history, advanced analytics, priority support |
| **Institutional** | ₹999/mo | Help Desk mode, multi-device, admin dashboard |

Additional Revenue:
- 🏛️ Government partnerships (Digital India initiative)
- 🏥 Hospital/Bank licensing deals
- 📚 Educational institution subscriptions

---

## SLIDE 15: Roadmap

| Phase | Timeline | Milestones |
|-------|----------|-----------|
| ✅ **v1.0** | Completed | Core translation, SOS, basic UI |
| ✅ **v2.0** | Completed | Kid Mode, Help Desk, Room DB |
| ✅ **v3.0** | Current | Dark Mode, Animations, Quiz, Learning |
| 🔜 **v4.0** | Q3 2026 | iOS version, Cloud sync, Advanced ML |
| 🔜 **v5.0** | Q4 2026 | Regional ISL dialects, Wearable support |

---

## SLIDE 16: Demo

**Live Demo / Screenshots**

*(Insert app screenshots or live demo video here)*

- Standard Mode Home Screen with Daily Sign
- Sign → Text translation in action
- Kid Mode Quiz
- Help Desk Session
- Emergency SOS with cooldown
- Dark Mode toggle

---

## SLIDE 17: Thank You

**SignTogether**
*Empowering Inclusivity Through Technology*

📧 mirmuzamil962@gmail.com
📧 simarkaur0217@gmail.com
📧

🤟 *"Every conversation matters. Let's make them all possible."*

---
