package com.importantnotification;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class to access app settings consistently across all components
 */
public class AppSettings {
    private static final String SETTINGS_PREFS = "ImportantNotificationSettings";
    
    // Default values from SettingsActivity
    public static final int DEFAULT_VOLUME_LEVEL = 8;
    public static final int DEFAULT_BEEP_COUNT = 15;
    public static final int DEFAULT_SMS_BEEP_DURATION = 400;
    public static final int DEFAULT_SMS_INTERVAL = 500;
    public static final int DEFAULT_CALL_BEEP_DURATION = 500;
    public static final int DEFAULT_CALL_INTERVAL = 600;
    public static final boolean DEFAULT_SERVICE_ENABLED = true;
    
    public static SharedPreferences getSettingsPrefs(Context context) {
        return context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
    }
    
    public static int getVolumeLevel(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("volume_level", DEFAULT_VOLUME_LEVEL);
    }
    
    public static int getBeepCount(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("beep_count", DEFAULT_BEEP_COUNT);
    }
    
    public static int getSmsBeepDuration(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("sms_beep_duration", DEFAULT_SMS_BEEP_DURATION);
    }
    
    public static int getSmsInterval(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("sms_interval", DEFAULT_SMS_INTERVAL);
    }
    
    public static int getCallBeepDuration(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("call_beep_duration", DEFAULT_CALL_BEEP_DURATION);
    }
    
    public static int getCallInterval(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getInt("call_interval", DEFAULT_CALL_INTERVAL);
    }
    
    public static boolean isServiceEnabled(Context context) {
        SharedPreferences prefs = getSettingsPrefs(context);
        return prefs.getBoolean("service_enabled", DEFAULT_SERVICE_ENABLED);
    }
    
    /**
     * Calculate the actual volume level for AudioManager based on user setting
     * @param context Application context
     * @param maxVolume Maximum volume from AudioManager.getStreamMaxVolume()
     * @return Calculated volume level
     */
    public static int calculateAlertVolume(Context context, int maxVolume) {
        int volumeLevel = getVolumeLevel(context); // 1-10 scale
        int alertVolume = Math.max((maxVolume * volumeLevel) / 10, 3); // Convert to actual volume, minimum of 3
        return alertVolume;
    }
}
