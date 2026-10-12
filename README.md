# ⚡ PumpX — Physical Proof of Work for Screen Time

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Version" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/CameraX-Real--Time_Vision-FF6F00?style=for-the-badge&logo=google&logoColor=white" alt="CameraX" />
  <img src="https://img.shields.io/badge/Persistence-Room_%2B_DataStore-009688?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" />
  <img src="https://img.shields.io/badge/Privacy-100%25_On--Device-00C853?style=for-the-badge&logo=shield&logoColor=white" alt="100% On-Device" />
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge" alt="License" />
</p>

---

## 💡 The Philosophy: "Physical Proof-of-Work"

> **Doomscrolling is frictionless. Fitness is full of friction. PumpX reverses the equation.**

Modern mobile apps (TikTok, Instagram, YouTube Shorts, X) are engineered by armies of behavioral psychologists to keep you scrolling. Standard screen-time blockers fail because clicking *"Ignore limit for 15 minutes"* costs **zero dopamine and zero effort**.

**PumpX introduces a biological firewall:**  
Your phone locks down addictive apps behind an unskippable paywall. The only currency accepted? **Push-ups verified in real-time by your phone’s camera via computer vision.**

No cheating. No waving your hand in front of the lens. No half-reps. **10 chest-to-deck push-ups = 15 minutes of unlocked screen time.**

---

## 🚀 Key Highlights & Architectural Capabilities

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PUMPX ARCHITECTURE                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                                                               
   [ Target App Launched ] ──────► [ BlockerService (Foreground Monitor) ]     
             │                                    │                            
             ▼                                    ▼                            
    UsageStats Poll (1s)              Is Daily Limit Exceeded?                 
                                            │                 │                
                                         [ YES ]            [ NO ]             
                                            │                 │                
                                            ▼                 ▼                
                                   [ Fullscreen Overlay ] [ Allow Usage ]      
                                   "LIMIT REACHED: 10 REPS"                    
                                            │                                  
                                            ▼                                  
                              [ CameraSessionScreen / CameraX ]                
                                            │                                  
                     ┌──────────────────────┴──────────────────────┐           
                     │                                             │           
                     ▼                                             ▼           
          [ Real-Time Frame Pipeline ]                 [ PoseOverlayCanvas ]   
                     │                                 (Live Skeleton & Angles)
                     ▼                                                         
          [ Angle & Landmark Math ]                                            
          • Left/Right Elbow Angles (Degrees)                                  
          • Torso Incline & Hip Alignment                                      
                     │                                                         
                     ▼                                                         
        [ PushupStateMachine (FSM) ]                                           
         UP ──► DESCENDING ──► DOWN ──► ASCENDING ──► REP_COUNTED              
                     │                                                         
                     ▼                                                         
           [ Target Reached! ] ──► [ Room Database Log ] ──► [ Unblock Timer ] 
                                   • Rep count logged        • 15 min granted  
                                   • Streak updated          • Notification    
```

---

## ⚡ Core Features

### 1. 🛡️ The Screen Time Blocker (Digital Discipline Engine)
- **App-Specific Enforcement:** Select exactly which apps trigger the lock (e.g., TikTok, Instagram, Reddit, YouTube, Mobile Legends).
- **Foreground Usage Tracking:** Lightweight background `BlockerService` monitors package activity using Android's `UsageStatsManager` with battery-friendly sampling.
- **Strict Lockdown Mode:** Intercepts blocked app launches with high-priority intents, preventing app launch until the physical tax is paid.
- **Earned-Time Token Economy:** Configure custom exchange rates:
  - Default: **10 Push-ups = 15 Minutes**
  - Hardcore: **20 Push-ups = 10 Minutes**
  - Savage: **50 Push-ups = 30 Minutes**
- **Emergency Cooldown System:** Built-in safeguards for genuine urgent communications without compromising daily discipline.

---

### 2. 👁️ Real-Time Computer Vision & Pose Analysis
- **100% On-Device Inference:** Zero video frames or photos leave the device. Camera frames are processed in-memory and immediately recycled.
- **CameraX ImageAnalysis Pipeline:** Low-latency 30 FPS stream processing with automatic camera switching (Front/Rear) and lens orientation compensation.
- **Biomechanical Angle Calculation:** Real-time 3D vector geometry calculating:
  $$\theta_{\text{elbow}} = \arccos\left(\frac{\vec{u} \cdot \vec{v}}{\|\vec{u}\| \|\vec{v}\|}\right)$$
  tracking shoulder-to-elbow and elbow-to-wrist vectors.
- **Four-Phase Finite State Machine (`PushupStateMachine`):**
  - **`UP`**: Locked elbows, plank position established.
  - **`DESCENDING`**: Continuous downward trajectory tracking.
  - **`DOWN`**: Elbow angle dips below calibrated depth threshold ($\le 90^\circ$).
  - **`ASCENDING`**: Upward return path validation.
  - **`REP_COUNTED`**: Clean lockout triggers rep increment, audio cue, and haptic feedback.
- **Anti-Cheat & Form Verification Engine:**
  - Prevents "head bobbing" cheating by evaluating shoulder-wrist displacement relative to hip displacement.
  - Flags hip sagging and arched backs using hip-to-ankle plane collinearity.
  - Cadence limiting: rejects reps under 400ms to eliminate sensor jitter and false positives.

---

### 3. 🎯 Dynamic Depth Calibration (`CalibrationScreen`)
- **Body Type Adaptability:** Everyone's limb length, camera angle, and floor distance are different.
- **Guided Zero-Point Setup:** Prompts the user into a starting plank to benchmark resting angles and camera distance.
- **Sensitivity Presets:**
  - **Beginner:** $100^\circ$ elbow flexion threshold.
  - **Standard:** $90^\circ$ right-angle chest flexion.
  - **Elite:** $80^\circ$ chest-to-floor strict military standard.

---

### 4. 🎨 Viral Social Post Studio (TikTok / IG Reel / Story Creator)
- **Built-in Visual Generator:** Export high-resolution proof-of-work cards directly to TikTok, Instagram Reels, X, and WhatsApp Status.
- **Multi-Ratio Framing Engine:**
  - **Story / Reel (9:16):** Full-screen mobile vertical visual with vibrant typography.
  - **Portrait (4:5):** Optimized for Instagram grid feeds.
  - **Square (1:1):** Classic social format.
  - **Landscape (16:9):** Web & horizontal previews.
- **6 Distinct Aesthetics:**
  - 🟢 **PumpX Dark Neon:** High-contrast cybernetic green on obsidian black.
  - 🔥 **Fire & Flame:** Crimson-orange gradient for hardcore training milestones.
  - ⚡ **Cyber Blue:** Clean electric cyan for futuristic minimalism.
  - 🦍 **Beast Mode:** Gold and deep bronze for streak celebrations.
  - 🌅 **Sunset Crimson:** Vivid purple and coral for evening grind sessions.
  - 🛡️ **Clean Titanium:** Monochromatic industrial aesthetic.
- **One-Click Viral Caption Generator:** Automatically formats post captions with stats and viral hashtags (`#PumpX #ProofOfWork #DopamineDetox #Discipline`).
- **Android FileProvider Integration:** Exports high-fidelity bitmap directly to the Android System Share Sheet with zero compression artifacts.

---

### 5. 📊 Gamification, Streaks & Local Telemetry
- **Daily Volume Tracking:** Real-time metrics tracking total reps, earned screen time, saved minutes, and consistency rate.
- **Streak Multiplier Engine:** Consecutive days of meeting limits maintain active flame streaks with milestone badges (3-day, 7-day, 30-day, 100-day).
- **Persistent Storage:** Built on Android Jetpack Room with Kotlin Symbol Processing (KSP) and asynchronous SQLite queries.
- **Reactive Settings:** Preferences backed by Jetpack DataStore, supporting dynamic Dark/Light/System theme toggles.

---

## 🛠️ Technology Stack & Dependencies

| Layer | Component | Version / Library |
| :--- | :--- | :--- |
| **Language** | Kotlin | `2.0.21` |
| **UI Framework** | Jetpack Compose | Material 3 BOM (`2024.10.01`) |
| **Vision & Camera** | CameraX | `androidx.camera:camera-camera2:1.4.0` |
| **Local Database** | Room Database | `androidx.room:room-ktx:2.6.1` (KSP) |
| **Preferences** | Jetpack DataStore | `androidx.datastore:datastore-preferences:1.1.1` |
| **Architecture** | MVVM + Clean Architecture | Android Jetpack Lifecycle & ViewModel Compose |
| **Navigation** | Navigation Compose | `androidx.navigation:navigation-compose:2.8.3` |
| **Asynchrony** | Coroutines & Flow | `kotlinx-coroutines-android:1.9.0` |
| **System Services** | Android OS APIs | `UsageStatsManager`, `ForegroundService`, `Vibrator` |
| **Build System** | Gradle (Kotlin DSL) | AGP `8.7.2`, Android API 36 (minSdk 24) |

---

## 📂 Project Architecture & Directory Layout

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Root host activity, edge-to-edge, navigation routing
├── PumpXApplication.kt                 # Application container initializing Room DB & Datastore
│
├── core/                                # Shared infrastructure & device bridges
│   ├── analytics/                       # Usage aggregation & metric computations
│   ├── datastore/                       # UserPreferencesRepository (Themes, Goals, Settings)
│   ├── navigation/                      # Screen sealed routes & argument definitions
│   ├── notifications/                  # Foreground service & reminder notifications
│   ├── permissions/                    # Camera & UsageStats runtime permission handlers
│   └── services/
│       └── BlockerService.kt           # Background foreground service monitoring active apps
│
├── data/                                # Data layer & persistent storage
│   ├── local/
│   │   ├── MonitoredAppDao.kt          # DAO for targeted applications
│   │   ├── MonitoredAppEntity.kt       # Entity table for target package names & limits
│   │   ├── PumpXDatabase.kt            # Room Database definition with KSP migrations
│   │   ├── PushupSessionDao.kt         # DAO for pushup session logs & rep history
│   │   └── PushupSessionEntity.kt      # Historical log of reps, duration, and apps unlocked
│   └── repository/
│       ├── MonitoredAppsRepository.kt  # Repository orchestrating monitored packages
│       └── PushupRepository.kt        # Repository aggregating volume & streak stats
│
├── presentation/                        # UI Presentation Layer (Jetpack Compose M3)
│   ├── about/                          # About, Legal, Terms & Privacy screens
│   ├── apps/                           # Monitored apps picker with installed packages filter
│   ├── blocker/                        # Fullscreen overlay intercepting limit breaches
│   ├── camera/                         # CameraX session, live pushup detector, calibration
│   ├── home/                           # Main dashboard (Daily progress, quick launch, studio)
│   ├── onboarding/                     # First-launch permission & value proposition flow
│   ├── settings/                       # Difficulty ratios, theme switcher, sensitivity
│   ├── share/                          # Social Post Studio (TikTok/IG customizable card generator)
│   └── stats/                          # Volume analytics, streak charts, app breakdowns
│
├── ui/theme/                            # Material 3 Design System
│   ├── Color.kt                        # Cyberpunk / Neon Green & Dark Charcoal palette
│   ├── Theme.kt                        # PumpXTheme wrapper with dynamic color adaptation
│   └── Type.kt                         # Typography definitions
│
└── vision/                              # Computer Vision & Biomechanics
    ├── PoseDetectorEngine.kt           # Frame processor calculating joint vectors & coordinates
    ├── PoseLandmarks.kt                # Data models for 3D landmarks & joint confidence scores
    ├── PoseOverlayCanvas.kt            # Real-time Compose Canvas drawing skeletal overlay
    ├── PushupState.kt                  # State enum (UP, DESCENDING, DOWN, ASCENDING, COUNTED)
    └── PushupStateMachine.kt           # Finite State Machine enforcing form rules & cheat rejection
```

---

## 🔒 Privacy & Permissions Manifest

PumpX adheres strictly to the principle of **Zero-Knowledge Hardware Execution**:

| Permission | Technical Requirement | User Privacy Guarantee |
| :--- | :--- | :--- |
| `android.permission.CAMERA` | Required for CameraX to stream frames into `PoseDetectorEngine`. | **Never recorded or streamed.** Frames are processed in volatile memory at 30 FPS and instantly cleared. |
| `android.permission.PACKAGE_USAGE_STATS` | Required by `UsageStatsManager` to detect when a monitored app is active. | Checked strictly on-device to enforce user-configured limits. No logs are exported. |
| `android.permission.SYSTEM_ALERT_WINDOW` | Allows displaying the blocking overlay when an app limit is exceeded. | Only displayed when a monitored package breaches its threshold. |
| `android.permission.FOREGROUND_SERVICE` | Keeps the discipline watchdog alive without being terminated by OEM battery savers. | Displays an ongoing persistent notification to guarantee transparency to the user. |
| `android.permission.POST_NOTIFICATIONS` | Delivers milestone rewards, streak reminders, and timer expirations. | Can be customized or muted at any time from in-app settings. |

---

## 🏃 Getting Started & Local Build

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer.
- **Android SDK:** `compileSdk 36`, `minSdk 24`.
- **JDK:** Java 11 or Java 17.
- **Physical Device:** Highly recommended (CameraX and `UsageStatsManager` require a physical camera and real package usage).

### Installation via CLI
```bash
# 1. Clone repository
git clone https://github.com/your-username/PumpX.git
cd PumpX

# 2. Check Gradle tasks
gradle tasks

# 3. Assemble Debug APK
gradle assembleDebug

# 4. Install onto connected ADB device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Granting Permissions via ADB (Fast Track)
For quick developer testing on an emulator or test device, you can grant the protected permissions directly via ADB:
```bash
# Grant Usage Access
adb shell pm grant com.aistudio.pumpx.app android.permission.PACKAGE_USAGE_STATS

# Grant Camera Access
adb shell pm grant com.aistudio.pumpx.app android.permission.CAMERA

# Grant Post Notifications
adb shell pm grant com.aistudio.pumpx.app android.permission.POST_NOTIFICATIONS
```

---

## 📸 The Social Post Studio in Action

When you hit a 100-push-up milestone or unlock your phone after a brutal workout, share your proof-of-work in seconds:

1. Tap **"STUDIO POSTS RÉSEAUX"** on the Home dashboard or Stats tab.
2. Select your platform aspect ratio (**9:16 Story / Reel**, **4:5 Post**, or **1:1 Square**).
3. Toggle presets (**TikTok Challenge**, **Beast Mode**, or **Custom Stats**).
4. Customize your handle (`@your_name`), reps completed, and motivational quote.
5. Hit **"Copier la Légende"** to copy the formatted caption to clipboard.
6. Hit **"Exporter & Partager"** to directly launch the TikTok / Instagram Stories share sheet!

---

## 🗺️ Engineering Roadmap

- [x] CameraX Real-Time Video Pipeline & Dynamic Canvas Overlay
- [x] Four-Phase Biomechanical Push-Up Finite State Machine
- [x] Anti-Cheat Angle & Cadence Validation
- [x] Background `BlockerService` with `UsageStatsManager`
- [x] Room Database Persistence with Daily & Weekly Aggregation
- [x] Social Share Studio with 6 Custom Themes & Multi-Ratio Export
- [ ] **Wear OS Companion App:** Haptic wrist vibration on rep completion & heart-rate tracking.
- [ ] **Squat & Pull-up Engines:** Expand from push-ups to full calisthenics proof-of-work.
- [ ] **Multiplayer Guilds:** Challenge friends to group screen-time lockouts (if anyone fails, everyone's TikTok locks).
- [ ] **AI Form Coach:** Gemini Nano integration offering contextual spoken audio advice on elbow flared angles and neck posture.

---

## 📄 License

```
Copyright 2026 PumpX Engineering Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

<p align="center">
  <b>Built for discipline. Powered by computer vision. Powered by you. 💥</b>
</p>
