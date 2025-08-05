package com.importantnotification.churchmode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing a Church Mode schedule.
 */
public class ChurchModeSchedule {
    private String id;
    private String name;
    private List<Integer> daysOfWeek; // 1=Sunday, 7=Saturday
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private String recurrence; // e.g. "weekly", "custom"

    public ChurchModeSchedule(String id, String name, List<Integer> daysOfWeek, 
                             int startHour, int startMinute, int endHour, int endMinute, String recurrence) {
        this.id = id;
        this.name = name;
        this.daysOfWeek = daysOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.recurrence = recurrence;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public int getStartHour() { return startHour; }
    public int getStartMinute() { return startMinute; }
    public int getEndHour() { return endHour; }
    public int getEndMinute() { return endMinute; }
    public String getRecurrence() { return recurrence; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public void setStartHour(int startHour) { this.startHour = startHour; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
    public void setEndHour(int endHour) { this.endHour = endHour; }
    public void setEndMinute(int endMinute) { this.endMinute = endMinute; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        
        JSONArray daysArray = new JSONArray();
        for (Integer day : daysOfWeek) {
            daysArray.put(day);
        }
        obj.put("daysOfWeek", daysArray);
        
        obj.put("startHour", startHour);
        obj.put("startMinute", startMinute);
        obj.put("endHour", endHour);
        obj.put("endMinute", endMinute);
        obj.put("recurrence", recurrence);
        return obj;
    }

    public static ChurchModeSchedule fromJson(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        
        JSONArray daysArray = obj.getJSONArray("daysOfWeek");
        List<Integer> daysOfWeek = new ArrayList<>();
        for (int i = 0; i < daysArray.length(); i++) {
            daysOfWeek.add(daysArray.getInt(i));
        }
        
        int startHour = obj.getInt("startHour");
        int startMinute = obj.getInt("startMinute");
        int endHour = obj.getInt("endHour");
        int endMinute = obj.getInt("endMinute");
        String recurrence = obj.getString("recurrence");
        
        return new ChurchModeSchedule(id, name, daysOfWeek, startHour, startMinute, endHour, endMinute, recurrence);
    }
}
