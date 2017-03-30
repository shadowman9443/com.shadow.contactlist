package com.shadow.contactlist;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shadow.contactlist.R;

import java.util.ArrayList;
import java.util.List;

public class AllContacts extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    RecyclerView rvContacts;
    database myDB;
    Boolean permission=false;
    int exist;
    private String mOrderBy = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;
    ContactListItem contactListItem;
    List<ContactListItem> contactList;
    AllContactsAdapter contactAdapter;
    Button showten;
    int n=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        myDB = new database(this);
        exist = myDB.tableExists();
        final TextView title = (TextView)findViewById(R.id.myname);
        showten= (Button) findViewById(R.id.showten);
        title.setText("Arafat Hossain Mozumder(Aunog)");
        ViewTreeObserver vto = title.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = title.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if(title.getLineCount() > 2){
                    Log.d("", "Line[" + title.getLineCount() + "]" + title.getText());
                    int lineEndIndex = title.getLayout().getLineEnd(1);
                    String text = title.getText().subSequence(0, lineEndIndex-2)+"...";
                    title.setText(text);
                    Log.d("","NewText:"+text);
                }

            }
        });
        contactList = new ArrayList();
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = checkContactsPermission();
            if(permission){
               if(exist==0)
                {
                    getLoaderManager().initLoader(1, null, this);
                    displayAllContacts();
                }
            }
        }
        else {
            if(exist == 0) {
                getLoaderManager().initLoader(1, null, this);
                displayAllContacts();
            }
        }

        displayAllContacts();

    }

    private void displayAllContacts() {

       //it will call constructor of databae class.
        // so the db will get created and also a table



            Cursor c = myDB.getAllData();
            if(c!=null && c.getCount()>0)

            {
                while (c.moveToNext()) {

                    String name = c.getString(1);
                    String phoneNo = c.getString(0);
                    contactListItem = new ContactListItem();
                    contactListItem.setContactName(name);
                    contactListItem.setContactNumber(phoneNo);
                    contactList.add(contactListItem);
                    if(contactList.size()==10){
                        break;
                    }
                }

            }

          contactAdapter = new AllContactsAdapter(contactList, getApplicationContext());
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(contactAdapter);



    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i==1) {
            return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, null, null, null,"upper("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    ContentResolver contentResolver = getContentResolver();
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneCursor.close();
                        myDB.addContact(name,phoneNumber);
                    }
                }
            }
            displayAllContacts();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
    public boolean checkContactsPermission() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            return false;
        } else {
            return true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {

                        exist = myDB.tableExists();
                        if(exist==0)
                        {
                            getLoaderManager().initLoader(1, null, this);
                        }
                      return;
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void showOther(View view) {

        n++;
        Cursor c = myDB.getAllDataten(n);
        if(c!=null && c.getCount()>0)

        {
            while (c.moveToNext()) {

                String name = c.getString(1);
                String phoneNo = c.getString(0);
                contactListItem = new ContactListItem();
                contactListItem.setContactName(name);
                contactListItem.setContactNumber(phoneNo);
                contactList.add(contactListItem);

            }

        }else{
            //showten.setBackgroundColor(Color.RED);
            showten.setText("No more results to show");
        }

        contactAdapter = new AllContactsAdapter(contactList, getApplicationContext());
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(contactAdapter);

    }
}
