# Important Notification App - Simple Specifications

## 1. Project Overview

### 1.1 Purpose
A simple personal-use Android app that ensures I never miss calls or texts from specific important contacts, even when my phone is in silent mode.

### 1.2 Target Platform
- **Android only** (my personal phone)
- **Minimum API level 26** (Android 8.0)

### 1.3 Core Goal
Override silent mode to play notification sound when receiving calls or SMS from designated important contacts.

## 2. Simple Functional Requirements

### 2.1 Contact Management
- **FR-001**: Select contacts from phone's contact list as "important"
- **FR-002**: View list of important contacts
- **FR-003**: Remove contacts from important list
- **FR-004**: Simple on/off toggle for the entire service

### 2.2 Notification Override
- **FR-005**: Detect incoming calls from important contacts
- **FR-006**: Detect incoming SMS from important contacts  
- **FR-007**: Override silent mode to play notification sound
- **FR-008**: Restore previous volume after notification

### 2.3 Background Operation
- **FR-009**: Run continuously in background
- **FR-010**: Restart after phone reboot
- **FR-011**: Minimal battery usage

## 3. Technical Implementation

### 3.1 Simple Architecture
- **MainActivity**: Contact selection and management UI
- **ImportantContactsService**: Background service for monitoring
- **PhoneStateListener**: Monitor incoming calls
- **SMS BroadcastReceiver**: Monitor incoming SMS
- **SharedPreferences**: Store important contacts list

### 3.2 Required Android Permissions
- `READ_PHONE_STATE`: Monitor incoming calls
- `RECEIVE_SMS`: Monitor incoming SMS
- `READ_CONTACTS`: Access contact picker
- `MODIFY_AUDIO_SETTINGS`: Override volume
- `FOREGROUND_SERVICE`: Run background service

### 3.3 Data Storage
- **SharedPreferences only**: Store contact phone numbers and app settings
- **No database**: Keep it simple
- **Local only**: No cloud storage or sync

## 4. Constraints & Simplifications

### 4.1 What We're NOT Building
- Complex UI/UX design
- Support for multiple devices
- Store submission
- Advanced features (scheduling, categories, etc.)
- Extensive testing across devices
- Cross-platform support
- Analytics or crash reporting

### 4.2 What We're Leveraging
- Android's built-in contact picker
- Standard PhoneStateListener
- Standard SMS BroadcastReceiver
- AudioManager for volume control
- SharedPreferences for storage

## 5. Success Criteria

### 5.1 Functional Success
- [ ] Can select important contacts easily
- [ ] Detects calls from important contacts
- [ ] Detects SMS from important contacts
- [ ] Overrides silent mode reliably
- [ ] Runs in background continuously
- [ ] Survives phone restarts

### 5.2 Personal Use Ready
- [ ] Works reliably on my phone
- [ ] Minimal battery impact
- [ ] Simple to use daily
- [ ] No crashes during normal use

## 6. Technical Notes

### 6.1 Phone Number Matching
- Store contacts as normalized phone numbers
- Handle basic format differences (+1, country codes)
- Simple string comparison (no complex matching)

### 6.2 Volume Override
- Save current volume before override
- Set to maximum volume when important contact detected
- Play default notification sound
- Restore previous volume after sound plays

### 6.3 Background Service
- Simple foreground service with persistent notification
- Register listeners in service onCreate()
- Restart service on boot via BroadcastReceiver

---

*This simplified specification focuses on personal use and getting a working app quickly rather than building a commercial product.*
