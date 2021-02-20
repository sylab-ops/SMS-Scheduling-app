package com.example.test.sms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class pendingTabFragment extends Fragment {
    private static final String TAG = "activity_pending_sms.xml";
    private SmsDatabaseHelper smsDatabaseHelper;
    private ListView list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_pending_sms, container, false);

        list = (ListView) view.findViewById(R.id.list_sms);

        // When Item pressed and holded delete Sms
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteSmsAndRefresh(view);
                return true;
            }
        });

        // Initialise the database
        smsDatabaseHelper = new SmsDatabaseHelper(getContext());

        populateList();
        return view;

    }

    // Detects is ui is visible to user.
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // If UI is visible then populate sms list
        if (this.isVisible()) {
            populateList();
        }
    }

    private void populateList() {
        // Start multi-thread to populate sms list
        PopulateAsyncTask task = new PopulateAsyncTask();
        task.execute();
    }


    private class PopulateAsyncTask extends AsyncTask<ArrayList<Sms>, Void, SmsAdapter> {
        @Override
        protected SmsAdapter doInBackground(ArrayList<Sms>... string) {
            // Get list of pending Sms from database helper
            ArrayList<Sms> sms = smsDatabaseHelper.getPendingSmsList();

            // Create a list adapter bound sms list
            SmsAdapter adapter = new SmsAdapter(getContext(), sms);
            return adapter;
        }

        protected void onPostExecute(SmsAdapter adapter) {
            super.onPostExecute(adapter);

            // Attach adapter to sms list view
            list.setAdapter(adapter);
        }
    }

    private void deleteSmsAndRefresh(View view) {

        // Get sms text values from child views.
        String name = ((TextView) view.findViewById(R.id.display_name)).getText().toString();
        String number = ((TextView) view.findViewById(R.id.display_number)).getText().toString();
        String messageDate = ((TextView) view.findViewById(R.id.display_messageDate)).getText().toString();
        String messageTime = ((TextView) view.findViewById(R.id.display_messageTime)).getText().toString();
        String message = ((TextView) view.findViewById(R.id.display_message)).getText().toString();
        String messageStatus = "Pending";

        DeleteAsyncTask task = new DeleteAsyncTask();
        task.execute(name, number, messageDate, messageTime, message, messageStatus);
    }


    private class DeleteAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... string) {

            // Set return message
            String returnMessage = "Unexpected Error";

            try {
                // Retrieve the ID of the SMS selected by the user
                int retrievedID = smsDatabaseHelper.retrieveSmsID(new Sms(string[0], string[1], string[2], string[3], string[4], string[5]));

                // Pass SMS ID and remove SMS from database
                smsDatabaseHelper.removeSms(retrievedID);

                // Cancel Alarm Using Retrieved SMS ID
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(getContext(), AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), retrievedID, intent, 0);
                alarmManager.cancel(pendingIntent);

                // Set return message to notify user
                returnMessage = "SMS Successfully Canceled";
            } catch (Exception e) {
                e.printStackTrace();
                returnMessage = "SMS Failed to Cancel";
            }
            return returnMessage;
        }

        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            // Display message to user.
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            // Refresh sms list
            populateList();
        }
    }
}
