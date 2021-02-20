package com.example.test.sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    public Context context;
    private SmsDatabaseHelper smsDatabaseHelper;
    private int SmsID;

    @Override
    public void onReceive(Context context, Intent intent) {
        setContext(context);
        // initialise database
        smsDatabaseHelper = new SmsDatabaseHelper(context);

        // Receive SmsID from alarm extra
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            SmsID = bundle.getInt("SmsID");
        }
        getSmsDetails();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    // Retrieve sms details from database using SmsID from alarm
    public void getSmsDetails() {
        ArrayList<Sms> sms = smsDatabaseHelper.getSmsByID(SmsID);
        String number = sms.get(0).number;
        String message = sms.get(0).message;
        String name = sms.get(0).name;
        sendMySMS(number, message, name);
    }

    // Send sms
    public void sendMySMS(String phoneNumber, String message, String name) {
        SmsManager sms = SmsManager.getDefault();
        // If message is too long for single sms split into multiple messages
        List<String> messages = sms.divideMessage(message);
        for (String msg : messages) {
            Intent intent = new Intent(getContext(), NotifyUser.class);

            // Pass recipient name and SmsID
            intent.putExtra("name", name);
            intent.putExtra("SmsID", SmsID);

            // Broadcast receiver to listen for the sentstatus of the message.
            PendingIntent sentIntent = PendingIntent.getBroadcast(getContext(), SmsID, intent, 0);
            sms.sendTextMessage(phoneNumber, null, msg, sentIntent, null);
        }
    }
}

