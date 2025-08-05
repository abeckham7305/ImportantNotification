package com.importantnotification.churchmode;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.importantnotification.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChurchModeActivity extends AppCompatActivity implements ChurchModeScheduleAdapter.OnScheduleActionListener {
    
    private RecyclerView scheduleList;
    private Button addScheduleButton;
    private ChurchModeScheduleAdapter adapter;
    private List<ChurchModeSchedule> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_church_mode);

        scheduleList = findViewById(R.id.schedule_list);
        addScheduleButton = findViewById(R.id.add_schedule_button);
        schedules = new ArrayList<>();

        adapter = new ChurchModeScheduleAdapter(schedules, this);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        scheduleList.setAdapter(adapter);

        addScheduleButton.setOnClickListener(v -> showAddEditDialog(null));

        loadSchedules();
    }

    private void loadSchedules() {
        SharedPreferences prefs = getSharedPreferences("church_mode_schedules", Context.MODE_PRIVATE);
        String json = prefs.getString("schedules", "[]");
        
        try {
            JSONArray arr = new JSONArray(json);
            schedules.clear();
            for (int i = 0; i < arr.length(); i++) {
                schedules.add(ChurchModeSchedule.fromJson(arr.getJSONObject(i)));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Toast.makeText(this, "Error loading schedules", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSchedules() {
        SharedPreferences prefs = getSharedPreferences("church_mode_schedules", Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        
        try {
            for (ChurchModeSchedule schedule : schedules) {
                arr.put(schedule.toJson());
            }
            prefs.edit().putString("schedules", arr.toString()).apply();
        } catch (JSONException e) {
            Toast.makeText(this, "Error saving schedules", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddEditDialog(ChurchModeSchedule schedule) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_schedule, null);
        
        EditText nameInput = dialogView.findViewById(R.id.schedule_name_input);
        CheckBox[] dayViews = {
            dialogView.findViewById(R.id.day_sun),
            dialogView.findViewById(R.id.day_mon),
            dialogView.findViewById(R.id.day_tue),
            dialogView.findViewById(R.id.day_wed),
            dialogView.findViewById(R.id.day_thu),
            dialogView.findViewById(R.id.day_fri),
            dialogView.findViewById(R.id.day_sat)
        };
        TimePicker startPicker = dialogView.findViewById(R.id.start_time_picker);
        TimePicker endPicker = dialogView.findViewById(R.id.end_time_picker);
        Spinner recurrenceSpinner = dialogView.findViewById(R.id.recurrence_spinner);

        // Populate fields if editing
        if (schedule != null) {
            nameInput.setText(schedule.getName());
            for (int i = 0; i < 7; i++) {
                dayViews[i].setChecked(schedule.getDaysOfWeek().contains(i + 1));
            }
            startPicker.setHour(schedule.getStartHour());
            startPicker.setMinute(schedule.getStartMinute());
            endPicker.setHour(schedule.getEndHour());
            endPicker.setMinute(schedule.getEndMinute());
            
            // Set recurrence spinner selection
            String[] recOptions = getResources().getStringArray(R.array.recurrence_options);
            for (int i = 0; i < recOptions.length; i++) {
                if (recOptions[i].equals(schedule.getRecurrence())) {
                    recurrenceSpinner.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button saveButton = dialogView.findViewById(R.id.save_schedule_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_schedule_button);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            List<Integer> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (dayViews[i].isChecked()) {
                    days.add(i + 1);
                }
            }
            int startHour = startPicker.getHour();
            int startMinute = startPicker.getMinute();
            int endHour = endPicker.getHour();
            int endMinute = endPicker.getMinute();
            String recurrence = recurrenceSpinner.getSelectedItem().toString();

            if (name.isEmpty() || days.isEmpty()) {
                if (name.isEmpty()) nameInput.setError("Name required");
                if (days.isEmpty()) dayViews[0].setError("Select at least one day");
                return;
            }

            if (schedule == null) {
                ChurchModeSchedule newSchedule = new ChurchModeSchedule(
                        UUID.randomUUID().toString(),
                        name, days, startHour, startMinute, endHour, endMinute, recurrence
                );
                schedules.add(newSchedule);
            } else {
                schedule.setName(name);
                schedule.setDaysOfWeek(days);
                schedule.setStartHour(startHour);
                schedule.setStartMinute(startMinute);
                schedule.setEndHour(endHour);
                schedule.setEndMinute(endMinute);
                schedule.setRecurrence(recurrence);
            }
            
            saveSchedules();
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onEdit(ChurchModeSchedule schedule) {
        showAddEditDialog(schedule);
    }

    @Override
    public void onDelete(ChurchModeSchedule schedule) {
        schedules.remove(schedule);
        saveSchedules();
        adapter.notifyDataSetChanged();
    }
}
