package com.example.test.sms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button newSmsButton, smsManagerButton, notificationInfoButton;
    private Switch notificationsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set background gradient to animate between gradient xml files
        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // Listen for swipes, if swiped left open sms schedule activity
        findViewById(R.id.main_layout).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, SmsScheduleActivity.class);
                startActivity(intent);
            }
        });

        // Initialise views
        newSmsButton = (Button) findViewById(R.id.newSmsButton);
        newSmsButton.setOnClickListener(this);

        smsManagerButton = (Button) findViewById(R.id.smsManagerButton);
        smsManagerButton.setOnClickListener(this);

        notificationInfoButton = (Button) findViewById(R.id.notificationInfoButton);
        notificationInfoButton.setOnClickListener(this);

        notificationsSwitch = (Switch) findViewById(R.id.notificationsSwitch);

        // Set initial value to true.
        SharedPreferences NotificationsPref = getSharedPreferences("switchStaus", 0);
        boolean notificationsStatusOn = NotificationsPref.getBoolean("notificationSwitch", true);

        // Set switch to value in shared preferences
        notificationsSwitch.setChecked(notificationsStatusOn);


        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // if notifications is checked display notifications on, if not display off
                if (notificationsSwitch.isChecked()){
                    Toast.makeText(getApplicationContext(), "Notifications turned on", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Notifications turned off", Toast.LENGTH_SHORT).show();
                }

                // Update shared preferences to switch checked status
                SharedPreferences NotificationsPref = getSharedPreferences("switchStaus", 0);
                SharedPreferences.Editor editor = NotificationsPref.edit();
                editor.putBoolean("notificationSwitch", isChecked);
                editor.commit();
            }
        });

    }

    @Override
    public void onClick(View v) {
        // On click for each button
        switch (v.getId()) {

            case R.id.newSmsButton:
                Intent intentNewSms = new Intent(MainActivity.this, SmsScheduleActivity.class);
                startActivity(intentNewSms);
                break;

            case R.id.smsManagerButton:
                Intent intentSmsManager = new Intent(MainActivity.this, SmsManagerActivity.class);
                startActivity(intentSmsManager);
                break;

            case R.id.notificationInfoButton:
                openDialog();
                break;

            default:
                break;
        }
    }

    private void openDialog(){
        // Display notifications Dialog
        NotificationInfoDialog notificationDialog =  new NotificationInfoDialog();
        notificationDialog.show(getSupportFragmentManager(), "NotificationInfoDialog");
    }

}
