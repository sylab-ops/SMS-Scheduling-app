package com.example.test.sms;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class failedTabFragment extends Fragment {

    private static final String TAG = "tab_sent_sms.xml";
    private SmsDatabaseHelper smsDatabaseHelper;
    private ListView list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_failed_sms, container, false);

        list = (ListView) view.findViewById(R.id.list_sms);

        // If item is clicked and holded display options
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {

                // Options for dialog
                String[] options = {"Delete", "Reschedule", "Cancel"};

                // Build dialog, set title and items as options
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select an option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedOption) {
                        // listen for selected item, check selected item and perform appropriate action
                        if (selectedOption == 0) {
                            deleteContactAndRefresh(view);
                        } else if (selectedOption == 1) {
                            rescheduleSMS(view);
                            deleteContactAndRefresh(view);
                        } else if (selectedOption == 2) {
                            // Do nothing as user has canceled
                        } else {
                            Toast.makeText(getContext(), "Sorry an error occurred.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

        // Initialise the database our SQLiteOpenHelper object
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

    // Retrieve sms information from selected item and pass to schedule sms act
    private void rescheduleSMS(View view) {
        String name = ((TextView) view.findViewById(R.id.display_name)).getText().toString();
        String number = ((TextView) view.findViewById(R.id.display_number)).getText().toString();
        String message = ((TextView) view.findViewById(R.id.display_message)).getText().toString();

        Intent intent = new Intent(getActivity(), SmsScheduleActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("number", number);
        intent.putExtra("message", message);
        startActivity(intent);
    }

    // Start multi-thread to populate list
    private void populateList() {
        PopulateAsyncTask task = new PopulateAsyncTask();
        task.execute();
    }

    private class PopulateAsyncTask extends AsyncTask<ArrayList<Sms>, Void, SmsAdapter> {
        @Override
        protected SmsAdapter doInBackground(ArrayList<Sms>... string) {
            //Get failed SMS list from database
            ArrayList<Sms> sms = smsDatabaseHelper.getFailedSmsList();

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
        String messageStatus = "Failed";

        // Start multi-threading for deleting sms
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

            // Refresh the list of SMS.
            populateList();
        }
    }
}
