package com.example.attendancetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<AttendanceRecord> {
    private Context context;
    private List<AttendanceRecord> records;

    public CustomAdapter(Context context, List<AttendanceRecord> records) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.attendance_item, parent, false);
        }

        AttendanceRecord record = records.get(position);

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);

        nameTextView.setText(record.getName());
        dateTextView.setText("Date: " + record.getDate());
        statusTextView.setText("Status: " + record.getStatus());

        // Set the status color based on the value
        if ("Present".equalsIgnoreCase(record.getStatus())) {
            statusTextView.setTextColor(context.getResources().getColor(R.color.colorPresent)); // Green
        } else if ("Absent".equalsIgnoreCase(record.getStatus())) {
            statusTextView.setTextColor(context.getResources().getColor(R.color.colorAbsent)); // Red
        }

        return convertView;
    }

}

