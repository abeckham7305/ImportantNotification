package com.importantnotification.churchmode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.importantnotification.R;

import java.util.List;

public class ChurchModeScheduleAdapter extends RecyclerView.Adapter<ChurchModeScheduleAdapter.ViewHolder> {

    public interface OnScheduleActionListener {
        void onEdit(ChurchModeSchedule schedule);
        void onDelete(ChurchModeSchedule schedule);
    }

    private final List<ChurchModeSchedule> schedules;
    private final OnScheduleActionListener listener;

    public ChurchModeScheduleAdapter(List<ChurchModeSchedule> schedules, OnScheduleActionListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView time;
        public TextView days;
        public ImageButton editButton;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.schedule_name);
            time = view.findViewById(R.id.schedule_time);
            days = view.findViewById(R.id.schedule_days);
            editButton = view.findViewById(R.id.edit_button);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_church_mode_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChurchModeSchedule schedule = schedules.get(position);
        holder.name.setText(schedule.getName());
        holder.time.setText(String.format("%02d:%02d - %02d:%02d", 
            schedule.getStartHour(), schedule.getStartMinute(), 
            schedule.getEndHour(), schedule.getEndMinute()));
        
        StringBuilder daysText = new StringBuilder();
        for (int i = 0; i < schedule.getDaysOfWeek().size(); i++) {
            if (i > 0) daysText.append(", ");
            daysText.append(dayIntToString(schedule.getDaysOfWeek().get(i)));
        }
        holder.days.setText(daysText.toString());
        
        holder.editButton.setOnClickListener(v -> listener.onEdit(schedule));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(schedule));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    private String dayIntToString(int day) {
        switch (day) {
            case 1: return "Sun";
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
            default: return "?";
        }
    }
}
