package com.importantnotification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private SharedPreferences prefs;
    
    // UI Components
    private SeekBar volumeSeekBar;
    private TextView volumeLabel;
    private NumberPicker beepCountPicker;
    private NumberPicker smsIntervalPicker;
    private NumberPicker callIntervalPicker;
    private Switch serviceEnabledSwitch;
    private Button saveButton;
    private Button resetButton;
    
    // Default values
    public static final int DEFAULT_VOLUME_LEVEL = 8; // Out of 10 (4/5 of max)
    public static final int DEFAULT_BEEP_COUNT = 15;
    public static final int DEFAULT_SMS_BEEP_DURATION = 400;
    public static final int DEFAULT_SMS_INTERVAL = 500;
    public static final int DEFAULT_CALL_BEEP_DURATION = 500;
    public static final int DEFAULT_CALL_INTERVAL = 600;
    public static final boolean DEFAULT_SERVICE_ENABLED = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("ImportantNotificationSettings", MODE_PRIVATE);
        
        // Initialize UI components
        initializeViews();
        
        // Load current settings
        loadSettings();
        
        // Set up listeners
        setupListeners();
    }
    
    private void initializeViews() {
        volumeSeekBar = findViewById(R.id.volume_seekbar);
        volumeLabel = findViewById(R.id.volume_label);
        beepCountPicker = findViewById(R.id.beep_count_picker);
        smsIntervalPicker = findViewById(R.id.sms_interval_picker);
        callIntervalPicker = findViewById(R.id.call_interval_picker);
        serviceEnabledSwitch = findViewById(R.id.service_enabled_switch);
        saveButton = findViewById(R.id.save_button);
        resetButton = findViewById(R.id.reset_button);
        
        // Configure number pickers
        beepCountPicker.setMinValue(1);
        beepCountPicker.setMaxValue(20);
        
        // Configure timing pickers for seconds (0.1 to 2.0 seconds)
        setupTimingPicker(smsIntervalPicker);
        setupTimingPicker(callIntervalPicker);
        
        // Configure volume seekbar
        volumeSeekBar.setMax(10);
    }
    
    private void setupTimingPicker(NumberPicker picker) {
        // Create array of decimal seconds from 0.1 to 2.0 in 0.1 increments
        String[] displayValues = new String[20];
        for (int i = 0; i < 20; i++) {
            double seconds = (i + 1) * 0.1;
            displayValues[i] = String.format("%.1f", seconds);
        }
        
        picker.setMinValue(0);
        picker.setMaxValue(19);
        picker.setDisplayedValues(displayValues);
        picker.setWrapSelectorWheel(false);
    }
    
    private int getPickerValueInMilliseconds(NumberPicker picker) {
        // Convert picker index to milliseconds
        // Index 0 = 0.1 sec = 100ms, Index 1 = 0.2 sec = 200ms, etc.
        return (picker.getValue() + 1) * 100;
    }
    
    private void setPickerValueFromMilliseconds(NumberPicker picker, int milliseconds) {
        // Convert milliseconds to picker index
        // 100ms = index 0, 200ms = index 1, etc.
        int index = (milliseconds / 100) - 1;
        index = Math.max(0, Math.min(19, index)); // Clamp to valid range
        picker.setValue(index);
    }
    
    private void loadSettings() {
        // Load volume setting
        int volumeLevel = prefs.getInt("volume_level", DEFAULT_VOLUME_LEVEL);
        volumeSeekBar.setProgress(volumeLevel);
        updateVolumeLabel(volumeLevel);
        
        // Load beep count
        int beepCount = prefs.getInt("beep_count", DEFAULT_BEEP_COUNT);
        beepCountPicker.setValue(beepCount);
        
        // Load timing settings (convert from milliseconds to picker values)
        int smsInterval = prefs.getInt("sms_interval", DEFAULT_SMS_INTERVAL);
        int callInterval = prefs.getInt("call_interval", DEFAULT_CALL_INTERVAL);
        setPickerValueFromMilliseconds(smsIntervalPicker, smsInterval);
        setPickerValueFromMilliseconds(callIntervalPicker, callInterval);
        
        // Load service enabled state
        boolean serviceEnabled = prefs.getBoolean("service_enabled", DEFAULT_SERVICE_ENABLED);
        serviceEnabledSwitch.setChecked(serviceEnabled);
    }
    
    private void setupListeners() {
        // Volume seekbar listener
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateVolumeLabel(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Save button listener
        saveButton.setOnClickListener(v -> saveSettings());
        
        // Reset button listener
        resetButton.setOnClickListener(v -> resetToDefaults());
    }
    
    private void updateVolumeLabel(int volume) {
        volumeLabel.setText("Alert Volume: " + volume + "/10");
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save all settings (convert timing values back to milliseconds)
        editor.putInt("volume_level", volumeSeekBar.getProgress());
        editor.putInt("beep_count", beepCountPicker.getValue());
        editor.putInt("sms_interval", getPickerValueInMilliseconds(smsIntervalPicker));
        editor.putInt("call_interval", getPickerValueInMilliseconds(callIntervalPicker));
        editor.putBoolean("service_enabled", serviceEnabledSwitch.isChecked());
        
        // Keep beep durations at their defaults (not user-configurable anymore)
        editor.putInt("sms_beep_duration", DEFAULT_SMS_BEEP_DURATION);
        editor.putInt("call_beep_duration", DEFAULT_CALL_BEEP_DURATION);
        
        editor.apply();
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Return to previous activity
        finish();
    }
    
    private void resetToDefaults() {
        volumeSeekBar.setProgress(DEFAULT_VOLUME_LEVEL);
        updateVolumeLabel(DEFAULT_VOLUME_LEVEL);
        beepCountPicker.setValue(DEFAULT_BEEP_COUNT);
        setPickerValueFromMilliseconds(smsIntervalPicker, DEFAULT_SMS_INTERVAL);
        setPickerValueFromMilliseconds(callIntervalPicker, DEFAULT_CALL_INTERVAL);
        serviceEnabledSwitch.setChecked(DEFAULT_SERVICE_ENABLED);
        
        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    // Helper methods for other classes to access settings
    public static int getVolumeLevel(SharedPreferences prefs) {
        return prefs.getInt("volume_level", DEFAULT_VOLUME_LEVEL);
    }
    
    public static int getBeepCount(SharedPreferences prefs) {
        return prefs.getInt("beep_count", DEFAULT_BEEP_COUNT);
    }
    
    public static int getSmsBeepDuration(SharedPreferences prefs) {
        return prefs.getInt("sms_beep_duration", DEFAULT_SMS_BEEP_DURATION);
    }
    
    public static int getSmsInterval(SharedPreferences prefs) {
        return prefs.getInt("sms_interval", DEFAULT_SMS_INTERVAL);
    }
    
    public static int getCallBeepDuration(SharedPreferences prefs) {
        return prefs.getInt("call_beep_duration", DEFAULT_CALL_BEEP_DURATION);
    }
    
    public static int getCallInterval(SharedPreferences prefs) {
        return prefs.getInt("call_interval", DEFAULT_CALL_INTERVAL);
    }
    
    public static boolean isServiceEnabled(SharedPreferences prefs) {
        return prefs.getBoolean("service_enabled", DEFAULT_SERVICE_ENABLED);
    }
}
