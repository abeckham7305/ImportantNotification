# Important Notification App

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Status](https://img.shields.io/badge/Status-Personal%20Project-blue.svg)

## 📱 Overview

A simple personal-use Android app that ensures you never miss calls or texts from specific important contacts, even when your phone is in silent mode. The app overrides system volume settings to play notification sounds for designated contacts.

## ✨ Simple Features

- **Contact Selection**: Pick important contacts from your phone's contact list
- **Silent Mode Override**: Bypass silent mode for important calls and texts
- **Background Operation**: Runs continuously with minimal battery impact
- **Simple UI**: Easy contact management with on/off toggle
- **Auto-Restart**: Automatically starts after phone reboot

## 🎯 Personal Use Cases

- Never miss emergency calls from family
- Stay reachable for urgent work communications  
- Ensure availability for elderly parents or children
- Sleep peacefully knowing important calls get through

## 🔧 Requirements

- **Android 8.0+** (API level 26 or higher)
- **Permissions**: Phone, Contacts, SMS, Audio settings
- **Storage**: ~10MB
- **Battery**: Minimal impact (designed for 24/7 operation)

## 📋 Documentation

- **[Simple Specifications](docs/SOFTWARE_SPECIFICATIONS.md)** - Basic requirements and technical details
- **[Development Plan](docs/DEVELOPMENT_PHASES.md)** - 4-5 week development timeline  
- **[TODO List](TODO.md)** - Simple task breakdown

## 🚀 Development Status

| Timeline | Focus | Status |
|----------|-------|--------|
| **Day 1 Morning** | Project Setup & Core Structure | 🚀 Ready to start! |
| **Day 1 Afternoon** | Detection Logic & Audio Override | 🤖 AI will code this |
| **Day 2 Morning** | Polish & Service Management | 🤖 AI will code this |
| **Day 2 Afternoon** | Test & Deploy on Your Phone | 🧪 We'll test together |

**Total Time**: ~2 days with AI assistance ("Vibe Coding")

## 🛠️ Simple Architecture

```
MainActivity 
├── Contact picker (Android built-in)
├── Important contacts list (SharedPreferences)
└── Service on/off toggle

ImportantContactsService (Background)
├── PhoneStateListener (call detection)
├── BroadcastReceiver (SMS detection)
└── AudioManager (volume override)
```

## 🔒 Privacy

- **Local Only**: All data stays on your device
- **No Cloud**: No external servers or data transmission
- **Minimal Permissions**: Only what's necessary for core function
- **Simple Storage**: Just phone numbers in SharedPreferences

## 📱 How It Works

1. **Setup**: Select important contacts from your phone
2. **Background**: App runs quietly in background
3. **Detection**: Monitors incoming calls and SMS
4. **Override**: If important contact detected, overrides silent mode
5. **Restore**: Returns volume to previous state after notification

## �️ Development Setup

```bash
# Clone and open in Android Studio
git clone https://github.com/yourusername/ImportantNotification.git

# Build and test
./gradlew assembleDebug
./gradlew installDebug
```

## � License

Personal use project - MIT License

## 🎯 Goals

- ✅ **AI-Powered**: Let AI write the code while you learn
- ✅ **Fast Results**: Working app in 2 days, not weeks  
- ✅ **Learn by Doing**: Understand Android development through real code
- ✅ **Vibe Coding**: Perfect practice for AI-assisted development
- ✅ **Personal Solution**: Solve a real problem you have

---

*Ready to build this together? Let's start coding! 🚀*
