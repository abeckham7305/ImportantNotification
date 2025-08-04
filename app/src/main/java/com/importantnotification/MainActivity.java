package com.importantnotification;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private Button toggleServiceBtn;
    private Button addContactBtn;
    private TextView serviceStatus;
    private LinearLayout contactsList;
    private SharedPreferences prefs;
    
    // Contact picker launcher
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        toggleServiceBtn = findViewById(R.id.toggle_service_btn);
        addContactBtn = findViewById(R.id.add_contact_btn);
        serviceStatus = findViewById(R.id.service_status);
        contactsList = findViewById(R.id.contacts_list);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("ImportantContacts", MODE_PRIVATE);
        
        // Set up contact picker
        setupContactPicker();
        
        // Set up button listeners
        setupButtonListeners();
        
        // Load and display contacts
        loadContacts();
        
        // Update service status
        updateServiceStatus();
        
        // Check permissions on startup
        checkPermissions();
    }
    
    private void setupContactPicker() {
        contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleContactSelection(result.getData());
                }
            }
        );
    }
    
    private void setupButtonListeners() {
        toggleServiceBtn.setOnClickListener(v -> toggleService());
        addContactBtn.setOnClickListener(v -> openContactPicker());
    }
    
    private void toggleService() {
        if (!hasRequiredPermissions()) {
            requestPermissions();
            return;
        }
        
        // For now, just show a toast - we'll implement the actual service next
        boolean isRunning = prefs.getBoolean("service_running", false);
        
        if (isRunning) {
            // Stop service
            prefs.edit().putBoolean("service_running", false).apply();
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        } else {
            // Start service
            prefs.edit().putBoolean("service_running", true).apply();
            Toast.makeText(this, "Service started (prototype mode)", Toast.LENGTH_SHORT).show();
        }
        
        updateServiceStatus();
    }
    
    private void openContactPicker() {
        if (!hasContactPermission()) {
            requestPermissions();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }
    
    private void handleContactSelection(Intent data) {
        Uri contactUri = data.getData();
        if (contactUri == null) return;
        
        try {
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                
                if (nameIndex >= 0 && numberIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);
                    
                    // Normalize phone number (remove spaces, dashes, etc.)
                    String normalizedNumber = normalizePhoneNumber(number);
                    
                    // Save contact
                    saveContact(name, normalizedNumber);
                    
                    // Refresh display
                    loadContacts();
                    
                    Toast.makeText(this, "Added: " + name, Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error adding contact", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveContact(String name, String number) {
        Set<String> contacts = prefs.getStringSet("important_contacts", new HashSet<>());
        Set<String> updatedContacts = new HashSet<>(contacts);
        updatedContacts.add(name + "|" + number);
        prefs.edit().putStringSet("important_contacts", updatedContacts).apply();
    }
    
    private void removeContact(String contactData) {
        Set<String> contacts = prefs.getStringSet("important_contacts", new HashSet<>());
        Set<String> updatedContacts = new HashSet<>(contacts);
        updatedContacts.remove(contactData);
        prefs.edit().putStringSet("important_contacts", updatedContacts).apply();
        loadContacts();
    }
    
    private void loadContacts() {
        contactsList.removeAllViews();
        
        Set<String> contacts = prefs.getStringSet("important_contacts", new HashSet<>());
        
        if (contacts.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No important contacts added yet");
            emptyView.setTextColor(getColor(android.R.color.darker_gray));
            emptyView.setPadding(16, 16, 16, 16);
            contactsList.addView(emptyView);
            return;
        }
        
        for (String contact : contacts) {
            addContactView(contact);
        }
    }
    
    private void addContactView(String contactData) {
        String[] parts = contactData.split("\\|");
        if (parts.length != 2) return;
        
        String name = parts[0];
        String number = parts[1];
        
        // Create a horizontal layout for each contact
        LinearLayout contactLayout = new LinearLayout(this);
        contactLayout.setOrientation(LinearLayout.HORIZONTAL);
        contactLayout.setPadding(16, 8, 16, 8);
        
        // Contact info
        TextView contactInfo = new TextView(this);
        contactInfo.setText(name + "\\n" + number);
        contactInfo.setTextSize(14);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        contactInfo.setLayoutParams(textParams);
        
        // Remove button
        Button removeBtn = new Button(this);
        removeBtn.setText("Remove");
        removeBtn.setTextSize(12);
        removeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Remove Contact")
                .setMessage("Remove " + name + " from important contacts?")
                .setPositiveButton("Remove", (dialog, which) -> removeContact(contactData))
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        contactLayout.addView(contactInfo);
        contactLayout.addView(removeBtn);
        contactsList.addView(contactLayout);
    }
    
    private void updateServiceStatus() {
        boolean isRunning = prefs.getBoolean("service_running", false);
        
        if (isRunning) {
            serviceStatus.setText("Running (Prototype)");
            serviceStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            toggleServiceBtn.setText("Stop Service");
        } else {
            serviceStatus.setText("Stopped");
            serviceStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            toggleServiceBtn.setText("Start Service");
        }
    }
    
    private String normalizePhoneNumber(String number) {
        // Remove all non-digits except +
        return number.replaceAll("[^+\\d]", "");
    }
    
    private boolean hasRequiredPermissions() {
        return hasContactPermission() && hasPhonePermission() && hasSmsPermission() && hasAudioPermission();
    }
    
    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
    
    private boolean hasPhonePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }
    
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    
    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void checkPermissions() {
        if (!hasRequiredPermissions()) {
            new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app needs access to your contacts, phone, SMS, and audio settings to work properly.")
                .setPositiveButton("Grant Permissions", (dialog, which) -> requestPermissions())
                .setNegativeButton("Later", null)
                .show();
        }
    }
    
    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        };
        
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions denied. App may not work correctly.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
