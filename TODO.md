# Important Notification App - Simple TODO List

## 📋 Project Setup (Week 1)

### Basic Android Project
- [ ] Create new Android project in Android Studio
- [ ] Set minimum SDK to API 26 (Android 8.0)
- [ ] Add required permissions to AndroidManifest.xml
  - [ ] `READ_PHONE_STATE`
  - [ ] `RECEIVE_SMS`
  - [ ] `READ_CONTACTS`
  - [ ] `MODIFY_AUDIO_SETTINGS`
  - [ ] `FOREGROUND_SERVICE`

### Simple Contact Management
- [ ] Create basic main activity with contact list
- [ ] Add "Add Contact" button that opens contact picker
- [ ] Store selected contacts in SharedPreferences
- [ ] Display list of important contacts with remove option
- [ ] Add simple on/off toggle for the service

## 🔧 Core Functionality (Week 2)

### Call Detection
- [ ] Create PhoneStateListener to detect incoming calls
- [ ] Extract caller phone number
- [ ] Compare with stored important contacts
- [ ] Trigger volume override on match

### SMS Detection
- [ ] Create BroadcastReceiver for SMS_RECEIVED
- [ ] Extract sender phone number from SMS
- [ ] Compare with stored important contacts
- [ ] Trigger volume override on match

### Contact Matching
- [ ] Implement simple phone number comparison
- [ ] Handle different phone number formats
- [ ] Store contacts as normalized phone numbers

## 🔊 Audio Override (Week 3)

### Volume Override System
- [ ] Use AudioManager to save current volume state
- [ ] Override volume to maximum when important contact detected
- [ ] Play default notification sound or ringtone
- [ ] Restore previous volume state after notification

### Testing & Polish
- [ ] Test call detection with phone in silent mode
- [ ] Test SMS detection with phone in silent mode
- [ ] Test with actual important contacts
- [ ] Add basic error handling for permission denials

## 🔄 Background Service (Week 4)

### Service Implementation
- [ ] Create simple foreground service
- [ ] Register PhoneStateListener in service
- [ ] Register SMS BroadcastReceiver in service
- [ ] Add persistent notification for foreground service
- [ ] Handle service lifecycle properly

### Reliability
- [ ] Add service restart on device boot
- [ ] Handle battery optimization settings
- [ ] Test service persistence over time
- [ ] Add simple crash recovery

## ✨ Final Polish (Week 5)

### UI Improvements
- [ ] Clean up main activity UI
- [ ] Add clear instructions for first-time setup
- [ ] Improve contact list display
- [ ] Add status indicator showing if service is running

### Optional Enhancements (if time permits)
- [ ] Custom notification sound selection
- [ ] Simple widget for quick enable/disable
- [ ] Basic notification when app successfully overrides silent mode
- [ ] Settings screen for volume level adjustment

## 🚀 Future Features & Enhancements
*Core functionality complete as of August 3, 2025 - Ready for additional features*

### 🎵 Audio & Sound Enhancements
- [ ] **Custom ringtones per contact** - Different sound for each important person
- [ ] **Sound volume customization** - Adjustable alert volume levels
- [ ] **Multiple tone patterns** - Different beep patterns (single, double, triple, long)
- [ ] **Escalating alerts** - Gradually increase volume if not acknowledged

### ⏰ Time-Based Features
- [ ] **Quiet hours support** - Respect Do Not Disturb schedules
- [ ] **Time-based rules** - Different behavior for day/night
- [ ] **Weekend/weekday modes** - Different important contacts for work vs personal time
- [ ] **Emergency escalation** - Multiple attempts with increasing urgency

### 📞 Call Management
- [ ] **Call override functionality** - Full implementation of PhoneStateReceiver alerts
- [ ] **Missed call alerts** - Special handling for missed calls from important contacts
- [ ] **Auto-callback options** - Quick callback buttons in notifications

### 👥 Contact Management
- [ ] **Contact groups** - Categories like "Family", "Work Emergency", "Medical"
- [ ] **Priority levels** - Different alert types for different importance levels
- [ ] **Contact sync** - Backup/restore important contacts list
- [ ] **Multiple numbers per contact** - Handle work/mobile/home numbers

### 📱 User Interface
- [ ] **Better contact management UI** - Drag-and-drop, bulk operations
- [ ] **Dashboard/status screen** - Recent alerts, service status, statistics  
- [ ] **Quick actions widget** - Home screen toggle and status
- [ ] **Dark mode support** - Better UI theming

### 🔧 Advanced Features
- [ ] **Battery optimization handling** - Better integration with power management
- [ ] **Accessibility improvements** - Screen reader support, larger text options
- [ ] **Backup & restore** - Export/import settings and contacts
- [ ] **Analytics dashboard** - Track alert frequency, effectiveness

### 🆕 Additional Ideas
*Space for new feature ideas as they come up during testing and use*

- [ ] **[ADD NEW IDEAS HERE]**
- [ ] 
- [ ] 
- [ ] 

## 🔴 Priority Bug Fixes & Issues
*Issues discovered during testing that need immediate attention*

### 🔐 Permission Management Issues
- [ ] **CRITICAL: Comprehensive permission prompting** 
  - Issue: Need to ensure ALL required permissions are properly requested and granted
  - Specific concern: "Do Not Disturb" access permission (ACCESS_NOTIFICATION_POLICY)
  - Current state: May not be guiding user through complete permission setup
  - Action needed: Review and improve permission request flow in MainActivity
  - User experience: Should walk user through each permission with clear explanations

### ⚡ Service On/Off Toggle Issues  
- [ ] **HIGH: Service toggle state persistence**
  - Issue: Service toggle may not properly persist off state
  - Observed behavior: User turned service off, but after reopening app it was back on
  - Current state: Toggle functionality may be resetting or not saving properly
  - Action needed: Investigate SharedPreferences persistence and service lifecycle
  - User experience: Toggle should reliably stay in the state user set it to

### 📋 Related Investigation Tasks
- [ ] Test permission flow from fresh install - verify all permissions requested
- [ ] Test toggle functionality - verify off state persists across app restarts
- [ ] Test service behavior when toggle is off - ensure service actually stops
- [ ] Review AndroidManifest.xml for complete permission declarations
- [ ] Check if we need additional permission request dialogs or settings redirects

### Final Testing
- [ ] Test complete flow: add contact → receive call/SMS → volume override
- [ ] Test after phone restart
- [ ] Test with phone in Do Not Disturb mode
- [ ] Test battery usage over a few days

---

## 🎯 Priority Levels

**� Must Have (Core Function)**
- Contact selection and storage
- Call and SMS detection
- Volume override functionality
- Background service

**🟡 Should Have (Reliability)**
- Service persistence and restart
- Basic error handling
- Battery optimization handling

**🟢 Nice to Have (Polish)**
- Custom sounds
- Better UI
- Status notifications
- Settings screen

---

## 📱 Technical Notes

### Simple Architecture
- **MainActivity**: Contact management UI
- **ImportantContactsService**: Background service for monitoring
- **SharedPreferences**: Store contact list and settings
- **PhoneStateListener**: Monitor incoming calls
- **BroadcastReceiver**: Monitor incoming SMS

### No Complex Features
- No database (just SharedPreferences)
- No complex UI (basic functional design)
- No advanced permissions handling
- No store submission preparation
- No multi-device testing

---

*This simplified TODO focuses on personal use rather than commercial deployment.*
