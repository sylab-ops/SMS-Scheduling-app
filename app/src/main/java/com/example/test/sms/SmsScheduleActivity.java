package com.example.test.sms;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class SmsScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private TextView displayTime;
    public TextView displayDate;
    private EditText messageInput, numberInput, nameInput;
    private Button selectTimeButton, scheduleButton, contactButton, selectDateButton;
    public int setHour = -1, setMinute = -1, setDay = -1, setMonth = -1, setYear = -1;
    private static final int REQUEST_SMS = 0;
    private static final int REQUEST_READ_CONTACTS = 3;
    Context context;
    private SmsDatabaseHelper smsDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_sms);

        context = this;

        // Listen for swipes, if swiped left open sms schedule activity, if swiped right open main activity
        findViewById(R.id.schedule_layout).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                Intent intent = new Intent(SmsScheduleActivity.this, MainActivity.class);
                startActivity(intent);
            }

            public void onSwipeLeft() {
                Intent intent = new Intent(SmsScheduleActivity.this, SmsManagerActivity.class);
                startActivity(intent);
            }
        });

        // Initialise views
        messageInput = (EditText) findViewById(R.id.messageInput);
        numberInput = (EditText) findViewById(R.id.numberInput);
        nameInput = (EditText) findViewById(R.id.nameInput);
        displayTime = findViewById(R.id.displayTime);
        displayDate = findViewById(R.id.displayDate);
        selectTimeButton = (Button) findViewById(R.id.selectTimeButton);
        selectDateButton = (Button) findViewById(R.id.selectDateButton);

        scheduleButton = findViewById(R.id.scheduleButton);
        contactButton = (Button) findViewById(R.id.contactButton);

        //Initialise database
        smsDatabaseHelper = new SmsDatabaseHelper(this);

        // Receive information form other activities
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // If bundle is not empty, set name and number to edit texts.
            String selectedName = bundle.getString("name");
            String selectedNumber = bundle.getString("number");
            updateEnterPhoneNumberEditText(selectedName, selectedNumber);

            // If bundle contains message, set message to edit text
            if (bundle.getString("message") != null) {
                String selectedMessage = bundle.getString("message");
                updateMessageEditText(selectedMessage);
            }
        }

        //set onClickListener for select Time Button
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new time picker from TimePickerFragment.
                DialogFragment timePicker = new TimePickerFragment();
                //S how time picker.
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        selectDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create new time picker from TimePickerFragment.
                DialogFragment datePicker = new DatePickerFragment();
                // Show date picker.
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });


        contactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                // If build version is marshmallow or higher request run time permission.
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // Check permission status of Read_Contacts
                    int ReadContactsPermission = checkSelfPermission(READ_CONTACTS);
                    // If permission is not granted display message informing user the application requires permission
                    if (ReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        requestContactPermission();
                        return;
                    }
                    selectContact();
                }
            }
        });


        //set onClickListener for schedule button
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If build version is marshmallow or higher request run time permission.
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // Check permission status of SEND_SMS
                    int SMSPermission = checkSelfPermission(SEND_SMS);
                    // If permission is not granted display message informing user the application requires permission
                    if (SMSPermission != PackageManager.PERMISSION_GRANTED) {
                        requestSmsPermission();
                        return;
                    }
                    validateInput();
                }
            }
        });
    }

    // Request permission to read users contacts
    private void requestContactPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }

    // Request permission to send SMS
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, REQUEST_SMS);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                // If result is permission granted attempt to schedule sms
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    validateInput();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Display message informing user they must allow permission, if user selects ok ask again
                        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You must allow this permission to schedule an SMS",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestSmsPermission();
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;

            case REQUEST_READ_CONTACTS:
                // If result is permission granted open select contact activity
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectContact();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Display message informing user they must allow permission, if user selects ok ask again
                        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                            showMessageOKCancel("You must allow this permission to select a contact",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestContactPermission();
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(SmsScheduleActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Set text of name input and number input edit texts
    private void updateEnterPhoneNumberEditText(String selectedName, String selectedNumber) {
        nameInput.setText(selectedName);
        numberInput.setText(selectedNumber);
    }

    // Set text of message input edit text
    private void updateMessageEditText(String selectedMessage) {
        messageInput.setText(selectedMessage);

        // Inform user they must select a new date and time
        Toast.makeText(getApplicationContext(), "Please select new date and time.", Toast.LENGTH_SHORT).show();
    }

    // Open select contact activity
    private void selectContact() {
        Intent intent = new Intent(SmsScheduleActivity.this, SelectContactActivity.class);
        startActivity(intent);
    }

    // Convert selected date and time to date format
    private Date convertSelectedDateTime() {
        Date convertedDateTime = null;
        String selectedDateTime = "";

        // Add 1 to month as months are between 0-11
        int selectedMonth = (setMonth + 1);

        // Join integers and convert to String
        selectedDateTime += setYear + "" + "" + String.format("%02d", selectedMonth) + "" + String.format("%02d", setDay) + "" + String.format("%02d", setHour) + "" + String.format("%02d", setMinute);

        try {
            //Convert String to date format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            convertedDateTime = sdf.parse(selectedDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDateTime;
    }


    private void validateInput() {
        // Assign input to variables
        final String contactName = nameInput.getText().toString();
        final String phoneNumber = numberInput.getText().toString();
        final String messageText = messageInput.getText().toString();

        if (contactName.isEmpty()) // Ensure name is not empty
        {
            Toast.makeText(getApplicationContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
        } else if (phoneNumber.isEmpty()) // Ensure phone number is not empty.
        {
            Toast.makeText(getApplicationContext(), "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
        } else if (messageText.isEmpty()) // Ensure message is not empty.
        {
            Toast.makeText(getApplicationContext(), "Please enter a message.", Toast.LENGTH_SHORT).show();
        } else if (setDay == -1 || setMonth == -1 || setYear == -1)  // Ensure date has been selected
        {
            Toast.makeText(getApplicationContext(), "Please select a date", Toast.LENGTH_SHORT).show();
        } else if (setHour == -1 || setMinute == -1) // Ensure a time has been selected.
        {
            Toast.makeText(getApplicationContext(), "Please select a time", Toast.LENGTH_SHORT).show();
        } else if (validateSelectedDateTime() == FALSE) // Compare dates, compare to returns negative number if selected date is less than current date
        {
            Toast.makeText(getApplicationContext(), "SMS must be scheduled for a future time", Toast.LENGTH_SHORT).show();

        } else if (AirplaneModeOn(getApplicationContext()) == TRUE) // Check if airplane mode is on
        {
            // Options for dialog
            String[] options = {"Continue to schedule", "Do not schedule", "Cancel"};

            // Build dialog, set title and items as options
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("SMS will not send in airplane mode. Please select an option:");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selectedOption) {
                    // listen for selected item, check selected item and perform appropriate action
                    if (selectedOption == 0) {
                        if (validateSelectedDateTime() == FALSE)  // Ensure time has not changed into past
                        {
                            Toast.makeText(getApplicationContext(), "Time has changed, SMS must be scheduled for a future time", Toast.LENGTH_SHORT).show();
                        } else {
                            addToSms(contactName, phoneNumber, messageText);
                        }
                    } else if (selectedOption == 1) {
                        Toast.makeText(context, "SMS has not been scheduled", Toast.LENGTH_LONG).show();
                        resetInput();
                    } else if (selectedOption == 2) {
                        //Do nothing as user has canceled

                    } else {
                        Toast.makeText(context, "Sorry an error occurred.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.show();
        } else //Schedule SMS
        {
            addToSms(contactName, phoneNumber, messageText);
        }
    }


    private static boolean AirplaneModeOn(Context context) {
        // Return true if airplane more is on
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    // Validate the selected date and time is in the future. Return True if date is in future
    private boolean validateSelectedDateTime() {
        Boolean validDateTime;
        // Get current date and time
        Date currentDateTime = Calendar.getInstance().getTime();
        // Get converted date and time
        Date selectedDateTime = convertSelectedDateTime();

        if (selectedDateTime.compareTo(currentDateTime) < 0) // Compare dates, returns negative number if selected date is less than current date
        {
            validDateTime = FALSE;
        } else {
            validDateTime = TRUE;
        }
        return validDateTime;
    }

    public void addToSms(String contactName, String phoneNumber, String messageText) {
        String name = contactName;
        String number = phoneNumber;
        String message = messageText;
        String messageDate = Integer.toString(setDay) + "/" + Integer.toString(setMonth) + "/" + Integer.toString(setYear); //Convert integers to string
        String messageTime = String.format("%02d:%02d", setHour, setMinute); // Convert to String and format hours and minutes
        String messageStatus = "Pending";

        // Start multi-thread to insert sms to database and start alarm manager
        ScheduleSmsAsyncTask task = new ScheduleSmsAsyncTask();
        task.execute(name, number, messageDate, messageTime, message, messageStatus);
    }

    private class ScheduleSmsAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... string) {
            String result = "SMS Successfully Scheduled";
            try {
                // Construct a Sms object and pass it to the helper for database insertion
                int SmsID = smsDatabaseHelper.addSms(new Sms(string[0], string[1], string[2], string[3], string[4], string[5]));

                // Create calendar with selected date and time
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, setYear);
                c.set(Calendar.MONTH, setMonth);
                c.set(Calendar.DAY_OF_MONTH, setDay);
                c.set(Calendar.HOUR_OF_DAY, setHour);
                c.set(Calendar.MINUTE, setMinute);
                c.set(Calendar.SECOND, 0);

                // Create alarm manager
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                // Pass SmsID to AlarmReceiver class
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.putExtra("SmsID", SmsID);

                //Set SmsID as unique id, Set time to calender, Start alarm
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), SmsID, intent, 0);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            } catch (Exception e) {
                e.printStackTrace();
                result = "SMS failed to schedule";
            }
            return result;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Display result message
            Toast.makeText(SmsScheduleActivity.this, result, Toast.LENGTH_SHORT).show();

            // Clear Sms Fields
            resetInput();
        }
    }

    // Reset fields
    private void resetInput() {
        nameInput.setText("");
        numberInput.setText("");
        messageInput.setText("");
        displayDate.setText("No Date Selected");
        displayTime.setText("No Time Selected");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // When user selects date assign date to variables
        setDay = dayOfMonth;
        setMonth = month;
        setYear = year;

        // Update the textView to display the date the user has selected
        updateDate();
    }

    private void updateDate() {
        String dateText = "Date: ";
        int displayMonth = (setMonth + 1); // Add 1 to month as months are between 0-11
        dateText += setDay + "/" + displayMonth + "/" + setYear;
        displayDate.setText(dateText); // Set selected date to text view
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        // When user picks time assign Time to variables
        setHour = hourOfDay;
        setMinute = minute;
        // Update the textView to display the time the user has selected
        updateTime();
    }

    private void updateTime() {
        // Update textView with time user has selected.
        String timeText = "Time: ";
        String time = String.format("%02d:%02d", setHour, setMinute);
        timeText += time;
        displayTime.setText(timeText);
    }

}
