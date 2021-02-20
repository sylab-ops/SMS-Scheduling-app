package com.example.test.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact> {
    public ContactAdapter(Context context, ArrayList<Contact> contact) {
        super(context, 0, contact);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the contacts item for this position.
        Contact contact = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_layout, parent, false);
        }
        // Initialise views.
        TextView display_contactName = (TextView) convertView.findViewById(R.id.display_contactName);
        TextView display_contactPhoneNumber = (TextView) convertView.findViewById(R.id.display_contactPhoneNumber);

        // set values of text views
        display_contactName.setText(contact.name);
        display_contactPhoneNumber.setText(contact.phoneNumber);

        // Return view to display on users screen
        return convertView;
    }

}