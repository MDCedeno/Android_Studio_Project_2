package com.example.attendancetracker;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    DatabaseHelper dbHelper; // Declare DatabaseHelper instance
    String selectedStatus = ""; // For storing selected status
    String selectedDate = ""; // For storing selected date
    ListView attendanceListView; // ListView for attendance records
    CustomAdapter adapter; // Custom adapter for the ListView

    // UI Components for filtering and searching
    Spinner dateRangeSpinner;
    EditText searchEditText;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Reference UI elements
        EditText nameEditText = findViewById(R.id.nameEditText);
        TextView dateTextView = findViewById(R.id.dateTextView);
        Spinner statusSpinner = findViewById(R.id.statusSpinner);
        Button addRecordButton = findViewById(R.id.addRecordButton);
        attendanceListView = findViewById(R.id.attendanceListView);

        // Set up Spinner for Status
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Spinner item selection listener for Status
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                selectedStatus = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedStatus = ""; // Default empty status
            }
        });

        // Set DatePickerDialog on Date TextView click
        dateTextView.setOnClickListener(v -> {
            // Get the current date
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int year = calendar.get(Calendar.YEAR);

            // Create and show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    DashboardActivity.this,
                    (view, year1, month1, dayOfMonth1) -> {
                        // Format selected date
                        selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;
                        dateTextView.setText(selectedDate); // Display selected date
                    },
                    year, month, dayOfMonth);
            datePickerDialog.show();
        });

        // Set OnClickListener for the Add Record Button
        addRecordButton.setOnClickListener(v -> {
            // Get the name input
            String name = nameEditText.getText().toString().trim();

            // Validate input fields
            if (name.isEmpty() || selectedDate.isEmpty() || selectedStatus.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert data into the database
            boolean isInserted = dbHelper.insertData(name, selectedDate, selectedStatus);

            // Show feedback
            if (isInserted) {
                Toast.makeText(this, "Record Added Successfully", Toast.LENGTH_SHORT).show();
                // Clear input fields
                nameEditText.setText("");
                dateTextView.setText("Select Date");
                statusSpinner.setSelection(0); // Reset to default
                fetchRecords(); // Refresh the records automatically
            } else {
                Toast.makeText(this, "Failed to Add Record", Toast.LENGTH_SHORT).show();
            }
        });

        // UI Components for filtering and searching
        dateRangeSpinner = findViewById(R.id.dateRangeSpinner);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        // Setup Date Range Spinner
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(
                this, R.array.date_range_array, android.R.layout.simple_spinner_item);
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(dateRangeAdapter);

        // Date range selection
        dateRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                // Handle date range selection
                if (position == 2) {  // Custom range selected
                    showDateRangePickerDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Search Button Logic
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchAttendance(query);
            } else {
                fetchRecords();
            }
        });

        fetchRecords(); // Automatically fetch records
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Automatically fetch records when the activity resumes
        fetchRecords();
    }

    // Method to fetch records from the database
    private void fetchRecords() {
        // Fetch the records from the database
        Cursor cursor = dbHelper.getAllData();

        if (cursor.getCount() == 0) {
            // No data found
            Toast.makeText(this, "No Attendance Records Found", Toast.LENGTH_SHORT).show();
        } else {
            // Create an ArrayList to hold the fetched records
            ArrayList<AttendanceRecord> attendanceList = new ArrayList<>();

            // Loop through the cursor to get the data
            while (cursor.moveToNext()) {
                String name = cursor.getString(1); // Get NAME column
                String date = cursor.getString(2); // Get DATE column
                String status = cursor.getString(3); // Get STATUS column

                // Add the attendance record to the list as an AttendanceRecord object
                attendanceList.add(new AttendanceRecord(name, status, date));
            }

            // Create an instance of the custom adapter
            adapter = new CustomAdapter(DashboardActivity.this, attendanceList);

            // Set the custom adapter to the ListView
            attendanceListView.setAdapter(adapter);

            // Set long click listener to delete records
            attendanceListView.setOnItemLongClickListener((parent, view, position, id) -> {
                // Get the selected record
                AttendanceRecord selectedRecord = attendanceList.get(position);
                showDeleteConfirmationDialog(selectedRecord); // Show confirmation dialog
                return true;
            });
        }
    }

    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog(AttendanceRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the record from the database
                    boolean isDeleted = dbHelper.deleteData(record.getName(), record.getDate());
                    if (isDeleted) {
                        Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();
                        fetchRecords(); // Refresh the records after deletion
                    } else {
                        Toast.makeText(this, "Failed to Delete Record", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Show Date Range Picker Dialog (for custom date range)
    private void showDateRangePickerDialog() {
        // Logic for picking custom start and end date
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            // Handle start date selection
            String startDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            // Prompt for end date
            DatePickerDialog endDateDialog = new DatePickerDialog(
                    DashboardActivity.this,
                    (view1, year1, month1, dayOfMonth1) -> {
                        String endDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;
                        filterByDateRange(startDate, endDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            endDateDialog.show();
        };

        DatePickerDialog startDateDialog = new DatePickerDialog(
                DashboardActivity.this, startDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        startDateDialog.show();
    }

    // Filter records by date range
    private void filterByDateRange(String startDate, String endDate) {
        Cursor cursor = dbHelper.getAttendanceByDateRange(startDate, endDate);
        ArrayList<AttendanceRecord> filteredList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String date = cursor.getString(2);
            String status = cursor.getString(3);
            filteredList.add(new AttendanceRecord(name, status, date));
        }
        adapter = new CustomAdapter(DashboardActivity.this, filteredList);
        attendanceListView.setAdapter(adapter);
    }

    // Method to search attendance records
    private void searchAttendance(String query) {
        Cursor cursor = dbHelper.searchAttendance(query);
        ArrayList<AttendanceRecord> searchResults = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String date = cursor.getString(2);
            String status = cursor.getString(3);
            searchResults.add(new AttendanceRecord(name, status, date));
        }
        adapter = new CustomAdapter(DashboardActivity.this, searchResults);
        attendanceListView.setAdapter(adapter);
    }
}
