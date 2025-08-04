package com.importantnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.NotificationCompat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.media.ToneGenerator;

import java.util.Set;
import java.util.HashSet;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String CHANNEL_ID = "important_sms";
    private static final int NOTIFICATION_ID = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "SMS Receiver triggered with action: " + intent.getAction());
        
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");
                
                Log.d(TAG, "Processing " + (pdus != null ? pdus.length : 0) + " SMS messages");
                
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                        } else {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        }
                        
                        if (smsMessage != null) {
                            String phoneNumber = smsMessage.getDisplayOriginatingAddress();
                            String messageBody = smsMessage.getDisplayMessageBody();
                            
                            Log.d(TAG, "SMS received from: " + phoneNumber + " with message: " + 
                                  (messageBody != null ? messageBody.substring(0, Math.min(messageBody.length(), 20)) + "..." : "null"));
                            handleIncomingSms(context, phoneNumber, messageBody);
                        }
                    }
                }
            }
        }
    }
    
    private void handleIncomingSms(Context context, String phoneNumber, String messageBody) {
        if (phoneNumber == null) return;
        
        String cleanNumber = cleanPhoneNumber(phoneNumber);
        Log.d(TAG, "SMS from: " + cleanNumber);
        
        // Check if this is from an important contact
        if (isImportantContact(context, cleanNumber)) {
            Log.d(TAG, "Important contact sent SMS! Creating alert.");
            handleImportantSms(context, cleanNumber, messageBody);
        }
    }
    
    private boolean isImportantContact(Context context, String phoneNumber) {
        SharedPreferences prefs = context.getSharedPreferences("ImportantContacts", Context.MODE_PRIVATE);
        Set<String> importantContacts = prefs.getStringSet("important_contacts", new HashSet<>());
        
        Log.d(TAG, "Checking if " + phoneNumber + " is in important contacts: " + importantContacts);
        
        // Check if the sender number matches any important contact
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
    
    private void handleImportantSms(Context context, String phoneNumber, String messageBody) {
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
        
        // If notification volume is 0, boost media volume instead to avoid ringer mode changes
        if (currentNotificationVolume == 0) {
            try {
                // Boost media volume for playing notification sound - use higher volume for better audibility
                int maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int alertVolume = Math.max(maxMediaVolume * 4 / 5, 3); // Use 4/5 of max volume, minimum of 3
                
                Log.d(TAG, "Boosting media volume from " + currentMediaVolume + " to " + alertVolume + " to avoid ringer mode changes");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, alertVolume, 0);
                
                // Schedule volume restoration
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
                }, 10000); // Restore after 10 seconds
                
            } catch (Exception e) {
                Log.e(TAG, "Error setting volume: " + e.getMessage());
                // Continue with notification even if volume boost fails
            }
        }
        
        // Create high-priority notification with sound
        showImportantSmsNotification(context, phoneNumber, messageBody);
        
        // Play notification sound using media stream
        playNotificationSoundWithMediaVolume(context);
        
        Log.d(TAG, "Important SMS alert created");
    }
    
    private void showImportantSmsNotification(Context context, String phoneNumber, String messageBody) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Important SMS",
                NotificationManager.IMPORTANCE_MAX  // Maximum importance
            );
            channel.setDescription("Notifications for SMS from important contacts");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setBypassDnd(true); // Bypass Do Not Disturb
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            // Set custom sound
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();
            channel.setSound(soundUri, audioAttributes);
            
            notificationManager.createNotificationChannel(channel);
        }
        
        // Truncate message if too long
        String displayMessage = messageBody;
        if (displayMessage != null && displayMessage.length() > 100) {
            displayMessage = displayMessage.substring(0, 100) + "...";
        }
        
        // Create extremely high-priority notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 Important SMS: " + phoneNumber)
            .setContentText(displayMessage)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(displayMessage))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(null, true)  // Try to show as heads-up
            .build();
        
        // Don't add FLAG_INSISTENT to prevent continuous vibration
        
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    private void playNotificationSound(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            android.media.Ringtone ringtone = RingtoneManager.getRingtone(context, notification);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification sound", e);
        }
    }
    
    private void playNotificationSoundWithMediaVolume(Context context) {
        try {
            // Use ToneGenerator to play tone through STREAM_MUSIC
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            
            // Play multiple beeps for more noticeable alert
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 800); // First beep
            
            Handler handler = new Handler();
            // Second beep after short pause
            handler.postDelayed(() -> {
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 800);
                } catch (Exception e) {
                    Log.e(TAG, "Error playing second beep", e);
                }
            }, 1000);
            
            // Third beep for even more attention
            handler.postDelayed(() -> {
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 800);
                } catch (Exception e) {
                    Log.e(TAG, "Error playing third beep", e);
                }
            }, 2000);
            
            // Clean up after all beeps are done
            handler.postDelayed(() -> {
                toneGenerator.release();
            }, 3500);
            
            Log.d(TAG, "Playing triple notification tone through media volume");
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification tone with media volume", e);
            // Fallback to regular method
            playNotificationSound(context);
        }
    }
    
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[^+\\d]", "");
    }
}
