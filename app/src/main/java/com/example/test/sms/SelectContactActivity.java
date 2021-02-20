package com.example.test.sms;

import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectContactActivity extends AppCompatActivity {
    ListView contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contactList = (ListView) findViewById(R.id.listview);

        // Get list of contacts
        ArrayList<Contact> contacts = getContacts();

        // Create a list adapter bound contact list
        ContactAdapter adapter = new ContactAdapter(getApplicationContext(), contacts);

        // attach contact list view adapter 
        contactList.setAdapter(adapter);

        // Set on click listener
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve name and number from list 
                String name = ((TextView) view.findViewById(R.id.display_contactName)).getText().toString();
                String number = ((TextView) view.findViewById(R.id.display_contactPhoneNumber)).getText().toString();

                // Pass name and number to schedule sms activity
                Intent intent = new Intent(SelectContactActivity.this, SmsScheduleActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("number", number);
                startActivity(intent);
            }
        });
    }


    public ArrayList<Contact> getContacts() {
        // Retrieve Contacts from phone
        Cursor result = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        // Create list of contact objects
        ArrayList<Contact> contact = new ArrayList<Contact>();

        // For number of contacts create a contact object with name and number.
        for (int i = 0; i < result.getCount(); i++) {
            result.moveToPosition(i);
            contact.add(new Contact(result.getString(result.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)), result.getString(result.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
        }
        return contact;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<Contact> contacts = getContacts();
        ContactAdapter adapter = new ContactAdapter(getApplicationContext(), contacts);
        contactList.setAdapter(adapter);
    }
}
