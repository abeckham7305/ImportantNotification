package com.importantnotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.importantnotification.churchmode.ChurchModeSchedule;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Utility class for Church Mode functionality
 * Handles checking if Church Mode is currently active based on saved schedules
 */
public class ChurchModeUtils {
    private static final String TAG = "ChurchModeUtils";
    private static final String PREFS_NAME = "church_mode_schedules";
    private static final String KEY_SCHEDULES = "schedules";

    /**
     * Check if Church Mode is currently active
     * @param context Application context
     * @return true if Church Mode is active and notifications should be suppressed
     */
    public static boolean isChurchModeActive(Context context) {
        try {
            List<ChurchModeSchedule> schedules = loadSchedules(context);
            Calendar now = Calendar.getInstance();
            
            int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            
            Log.d(TAG, "Checking Church Mode status at " + currentHour + ":" + 
                  String.format("%02d", currentMinute) + " on day " + currentDayOfWeek);
            
            for (ChurchModeSchedule schedule : schedules) {
                if (isScheduleActiveNow(schedule, currentDayOfWeek, currentHour, currentMinute)) {
                    Log.d(TAG, "Church Mode ACTIVE: " + schedule.getName() + 
                          " (" + formatTime(schedule.getStartHour(), schedule.getStartMinute()) + 
                          " - " + formatTime(schedule.getEndHour(), schedule.getEndMinute()) + ")");
                    return true;
                }
            }
            
            Log.d(TAG, "Church Mode inactive - no matching schedules");
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking Church Mode status", e);
            return false; // Default to allowing notifications if error occurs
        }
    }

    /**
     * Check if a specific schedule is active right now
     */
    private static boolean isScheduleActiveNow(ChurchModeSchedule schedule, int currentDayOfWeek, int currentHour, int currentMinute) {
        // Check if today is selected for this schedule
        List<Integer> selectedDays = schedule.getDaysOfWeek();
        if (!selectedDays.contains(currentDayOfWeek)) {
            return false;
        }
        
        // Convert times to minutes for easier comparison
        int currentTimeInMinutes = currentHour * 60 + currentMinute;
        int startTimeInMinutes = schedule.getStartHour() * 60 + schedule.getStartMinute();
        int endTimeInMinutes = schedule.getEndHour() * 60 + schedule.getEndMinute();
        
        // Check if current time is within the schedule window
        boolean isActive;
        if (endTimeInMinutes > startTimeInMinutes) {
            // Normal case: start and end on same day
            isActive = currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes;
        } else {
            // Edge case: schedule crosses midnight (e.g., 11:00 PM to 1:00 AM)
            isActive = currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes;
        }
        
        if (isActive) {
            Log.d(TAG, "Schedule '" + schedule.getName() + "' is active: " + 
                  formatTime(schedule.getStartHour(), schedule.getStartMinute()) + 
                  " - " + formatTime(schedule.getEndHour(), schedule.getEndMinute()) + 
                  " (current: " + formatTime(currentHour, currentMinute) + ")");
        }
        
        return isActive;
    }

    /**
     * Format time as HH:mm string
     */
    private static String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Load schedules from SharedPreferences
     */
    private static List<ChurchModeSchedule> loadSchedules(Context context) {
        List<ChurchModeSchedule> schedules = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String schedulesJson = prefs.getString(KEY_SCHEDULES, "[]");
        
        try {
            JSONArray arr = new JSONArray(schedulesJson);
            for (int i = 0; i < arr.length(); i++) {
                ChurchModeSchedule schedule = ChurchModeSchedule.fromJson(arr.getJSONObject(i));
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading schedules", e);
        }
        
        Log.d(TAG, "Loaded " + schedules.size() + " Church Mode schedules");
        return schedules;
    }

    /**
     * Get day name for logging
     */
    private static String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "Unknown";
        }
    }
}
