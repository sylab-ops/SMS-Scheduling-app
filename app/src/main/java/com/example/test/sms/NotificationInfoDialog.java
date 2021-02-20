package com.example.test.sms;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class NotificationInfoDialog extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build notification, set title, message and button listener.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Notifications")
                .setMessage("Notifications will only be sent to provide information on the sent status of your scheduled SMS.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // hide dialog when clicked
                    }
                });
        return builder.create();
    }
}

