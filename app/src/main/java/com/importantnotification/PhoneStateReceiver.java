package com.importantnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.media.AudioManager;
import android.util.Log;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;
import android.os.Handler;
import androidx.core.app.NotificationCompat;
import android.media.ToneGenerator;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.net.Uri;

import java.util.Set;
import java.util.HashSet;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneStateReceiver";
    private static final String CHANNEL_ID = "important_calls";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        
        Log.d(TAG, "Phone state changed: " + state + ", Number: " + phoneNumber);
        
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            if (phoneNumber == null) {
                Log.w(TAG, "Phone number is null - this may be due to Android privacy restrictions on API 28+");
                Log.d(TAG, "Checking if app has READ_CALL_LOG permission to access caller ID");
                
                // Even if we can't get the number, we can still show an alert for any incoming call
                // and let the user decide. For now, let's log this and not trigger false alerts.
                handleIncomingCallWithoutNumber(context);
            } else {
                handleIncomingCall(context, phoneNumber);
            }
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            handleCallEnded(context);
        }
    }
    
    private void handleIncomingCallWithoutNumber(Context context) {
        Log.d(TAG, "Incoming call detected but caller ID is hidden or restricted");
        // For now, we won't trigger alerts for calls without caller ID to avoid false positives
        // In the future, we could implement a setting to allow alerts for all incoming calls
    }
    
    private void handleIncomingCall(Context context, String phoneNumber) {
        if (phoneNumber == null) return;
        
        // Clean the phone number (remove formatting)
        String cleanNumber = cleanPhoneNumber(phoneNumber);
        Log.d(TAG, "Incoming call from: " + cleanNumber);
        
        // Check if this is an important contact
        if (isImportantContact(context, cleanNumber)) {
            Log.d(TAG, "Important contact calling! Overriding silent mode.");
            overrideSilentMode(context, cleanNumber);
        }
    }
    
    private void playCallAlertSound(Context context) {
        try {
            // TODO: FUTURE ENHANCEMENT - Add ability to stop beeping when:
            // 1. Call is answered by user
            // 2. Call is declined/ended
            // 3. Call notification is acknowledged/dismissed
            // This will require tracking active beep sequences and implementing cancellation mechanism
            // Consider using TelephonyManager.CALL_STATE_OFFHOOK to detect call answered
            
            // Use ToneGenerator to play tone through STREAM_MUSIC
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            
            Handler handler = new Handler();
            
            // Get user settings for call alerts
            int beepCount = AppSettings.getBeepCount(context);
            int beepInterval = AppSettings.getCallInterval(context);
            
            // Use fixed beep duration (a beep is a beep!)
            final int BEEP_DURATION = 500; // Original call beep duration
            
            Log.d(TAG, "Playing " + beepCount + " call beeps (500ms duration, " + beepInterval + "ms intervals)");
            
            // Play beeps with user-configured settings
            for (int i = 0; i < beepCount; i++) {
                final int beepNumber = i + 1;
                handler.postDelayed(() -> {
                    try {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION);
                        Log.d(TAG, "Playing call beep " + beepNumber + " of " + beepCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error playing call beep " + beepNumber, e);
                    }
                }, i * beepInterval);
            }
            
            // Clean up after all beeps are done
            handler.postDelayed(() -> {
                toneGenerator.release();
                Log.d(TAG, "Call alert sequence complete - " + beepCount + " beeps finished");
            }, beepCount * beepInterval + 1000);
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing call alert tone", e);
        }
    }
    
    private void handleCallEnded(Context context) {
        // Call ended - no special handling needed with new approach
        Log.d(TAG, "Call ended");
    }
    
    private boolean isImportantContact(Context context, String phoneNumber) {
        SharedPreferences prefs = context.getSharedPreferences("ImportantContacts", Context.MODE_PRIVATE);
        Set<String> importantContacts = prefs.getStringSet("important_contacts", new HashSet<>());
        
        Log.d(TAG, "Checking if " + phoneNumber + " is in important contacts: " + importantContacts);
        
        // Check if the caller number matches any important contact
        for (String contact : importantContacts) {
            // Contact format is "Name|PhoneNumber"
            if (contact.contains("|")) {
                String storedNumber = contact.split("\\|")[1];
                Log.d(TAG, "Comparing " + phoneNumber + " with stored " + storedNumber);
                
                if (phoneNumber != null && storedNumber != null) {
                    // Clean both numbers for comparison
                    String cleanIncoming = cleanPhoneNumber(phoneNumber);
                    String cleanStored = cleanPhoneNumber(storedNumber);
                    
                    // Check various number formats
                    if (cleanIncoming.equals(cleanStored) ||
                        cleanIncoming.endsWith(cleanStored.replaceAll("^\\+?1", "")) ||
                        cleanStored.endsWith(cleanIncoming.replaceAll("^\\+?1", ""))) {
                        Log.d(TAG, "Match found! " + contact);
                        return true;
                    }
                }
            }
        }
        
        Log.d(TAG, "No match found for " + phoneNumber);
        return false;
    }
    
    private void overrideSilentMode(Context context, String phoneNumber) {
        // Check if service is enabled
        if (!AppSettings.isServiceEnabled(context)) {
            Log.d(TAG, "Service disabled in settings, skipping call alert");
            return;
        }
        
        // Check if Church Mode is active
        if (ChurchModeUtils.isChurchModeActive(context)) {
            Log.d(TAG, "Church Mode is active - suppressing notification for call from " + phoneNumber);
            return;
        }
        
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        
        // Check if phone is in silent or vibrate mode
        int currentRingerMode = audioManager.getRingerMode();
        int currentNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int currentMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        Log.d(TAG, "Current ringer mode: " + currentRingerMode + ", notification volume: " + currentNotificationVolume + ", media volume: " + currentMediaVolume);
        
        // Store original settings
        final int originalNotificationVolume = currentNotificationVolume;
        final int originalMediaVolume = currentMediaVolume;
        final int originalRingerMode = currentRingerMode;
        
        // If notification volume is 0, boost media volume for call alert
        if (currentNotificationVolume == 0) {
            try {
                // Use settings-based volume calculation
                int maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int alertVolume = AppSettings.calculateAlertVolume(context, maxMediaVolume);
                
                Log.d(TAG, "Boosting media volume from " + currentMediaVolume + " to " + alertVolume + " for call alert");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, alertVolume, 0);
                
                // Calculate when all beeps will be finished
                int beepCount = AppSettings.getBeepCount(context);
                int beepInterval = AppSettings.getCallInterval(context);
                int totalBeepTime = beepCount * beepInterval + 500; // Add beep duration
                int volumeRestoreDelay = totalBeepTime + 3000; // 3 seconds after beeps end
                
                Log.d(TAG, "Will restore volume in " + volumeRestoreDelay + "ms (after " + beepCount + " beeps finish)");
                
                // Schedule volume restoration after beeps complete
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    try {
                        Log.d(TAG, "Restoring media volume to " + originalMediaVolume);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0);
                        
                        // Ensure ringer mode hasn't changed
                        if (audioManager.getRingerMode() != originalRingerMode) {
                            Log.d(TAG, "Correcting ringer mode back to " + originalRingerMode);
                            audioManager.setRingerMode(originalRingerMode);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error restoring audio settings", e);
                    }
                }, volumeRestoreDelay);
                
            } catch (Exception e) {
                Log.e(TAG, "Error setting volume: " + e.getMessage());
            }
        }
        
        // Create high-priority notification 
        String contactName = getContactNameFromNumber(context, phoneNumber);
        showImportantCallNotification(context, contactName);
        
        // Play call alert sound using media stream
        playCallAlertSound(context);
        
        Log.d(TAG, "Important call alert created for " + contactName);
    }
    
    private void showImportantCallNotification(Context context, String contactName) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Important Calls",
                NotificationManager.IMPORTANCE_MAX  // Maximum importance
            );
            channel.setDescription("Notifications for calls from important contacts");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setBypassDnd(true); // Bypass Do Not Disturb
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Create high-priority notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📞 Important Call: " + contactName)
            .setContentText("Silent mode overridden for incoming call")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(null, true)  // Try to show as heads-up
            .build();
        
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        // Remove all non-digit characters except +
        return phoneNumber.replaceAll("[^+\\d]", "");
    }
    
    /**
     * Get the contact name for a phone number from the device's contacts
     */
    private String getContactNameFromNumber(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.d(TAG, "Contact lookup: phone number is null or empty");
            return "Unknown Contact";
        }
        
        Log.d(TAG, "Looking up contact name for: " + phoneNumber);
        
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, 
                                          Uri.encode(phoneNumber));
            Log.d(TAG, "Contact lookup URI: " + uri.toString());
            
            Cursor cursor = context.getContentResolver().query(uri, 
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, 
                null, null, null);
                
            if (cursor != null) {
                Log.d(TAG, "Contact query returned " + cursor.getCount() + " results");
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    cursor.close();
                    Log.d(TAG, "Found contact name: " + name);
                    return (name != null && !name.trim().isEmpty()) ? name : phoneNumber;
                }
                cursor.close();
                Log.d(TAG, "No contact found for number: " + phoneNumber);
            } else {
                Log.d(TAG, "Contact query returned null cursor");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error looking up contact name: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Return the phone number if we can't find a contact name
        Log.d(TAG, "Returning phone number as fallback: " + phoneNumber);
        return phoneNumber;
    }
}
