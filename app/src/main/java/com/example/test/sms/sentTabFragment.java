package com.example.test.sms;

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

public class sentTabFragment extends Fragment {

    private static final String TAG = "tab_sent_sms.xml";

    private SmsDatabaseHelper smsDatabaseHelper;
    private ListView list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_sent_sms, container, false);

        list = (ListView) view.findViewById(R.id.list_sms);

        // When Item pressed and holded delete Sms
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteContactAndRefresh(view);
                return true;
            }
        });


        // Initialise database
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
        // Start multi-threading to populate sms list
        PopulateAsyncTask task = new PopulateAsyncTask();
        task.execute();
    }


    private class PopulateAsyncTask extends AsyncTask<ArrayList<Sms>, Void, SmsAdapter> {
        @Override
        protected SmsAdapter doInBackground(ArrayList<Sms>... string) {
            // Get sent SMS list from database
            ArrayList<Sms> sms = smsDatabaseHelper.getSentSmsList();

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

    private void deleteContactAndRefresh(View view) {
        // Get sms text values from child views.
        String name = ((TextView) view.findViewById(R.id.display_name)).getText().toString();
        String number = ((TextView) view.findViewById(R.id.display_number)).getText().toString();
        String messageDate = ((TextView) view.findViewById(R.id.display_messageDate)).getText().toString();
        String messageTime = ((TextView) view.findViewById(R.id.display_messageTime)).getText().toString();
        String message = ((TextView) view.findViewById(R.id.display_message)).getText().toString();

        String messageStatus = "Sent";

        // Start multi-thread to delete sms
        DeleteAsyncTask task = new DeleteAsyncTask();
        task.execute(name, number, messageDate, messageTime, message, messageStatus);
    }

    private class DeleteAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... string) {
            String returnMessage = "Unexpected Error";
            try {
                // Search database using Sms details to retrieve the id
                int retrievedID = smsDatabaseHelper.retrieveSmsID(new Sms(string[0], string[1], string[2], string[3], string[4], string[5]));
                // Use ID to remove from the database
                smsDatabaseHelper.removeSms(retrievedID);
                returnMessage = "SMS Succesfully Deleted";
            } catch (Exception e) {
                e.printStackTrace();
                returnMessage = "Could not delete SMS";
            }
            return returnMessage;
        }

        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            // Display if delete was successful
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            // Refresh sms list
            populateList();

        }
    }
}
