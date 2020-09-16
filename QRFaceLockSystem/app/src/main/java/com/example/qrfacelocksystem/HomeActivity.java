package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Install the Java helper library from twilio.com/docs/libraries/java
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private long exitTime = 0;

    private FirebaseAuth mAuth;
    private FirebaseUser users;

    public static final String ACCOUNT_SID = "ACfeb3175660e8f538c64caa1e9d82b912";
    public static final String AUTH_TOKEN = "4f3da5ddb75fbf38318886b157f37a24";


    private String username_shared, email_shared, uid_shared, door_lock_shared, device_name_shared, phone_shared;
    private Boolean lock_status_shared;

    private int currentDoorId;
    private String uid_db, username_db, email_db, password_db, door_lock_db, device_name_db, phone_db;
    private boolean lock_status_db;

    private ActionBar actionBar;

    private Spinner choose_Door;
    private ArrayAdapter<String> adpater;
    private ArrayList<String> spinnerDataList;
    private ValueEventListener listener;

    private Button unlockBtn, lockBtn, historyBtn;
    private Button logout;

    private TextView welcomeLabel, doorCodeLabel, doorResultLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        users = mAuth.getCurrentUser();

        choose_Door = (Spinner)findViewById(R.id.chooseDoor);
        spinnerDataList = new ArrayList<>();
        adpater = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_spinner_dropdown_item,spinnerDataList);
        choose_Door.setAdapter(adpater);


        unlockBtn = (Button) findViewById(R.id.unlock_button);
        lockBtn = (Button) findViewById(R.id.lock_button);
        historyBtn = (Button) findViewById(R.id.history_button);

        welcomeLabel = (TextView)findViewById(R.id.welcomeSlogan);
        doorCodeLabel = (TextView)findViewById(R.id.doorID);
        doorResultLabel = (TextView)findViewById(R.id.doorStatusResult);



//        retrieveCurrentData_Firebase();

        selectedItem();

        retrieveAllDoor_Firebase();


        lockBtn_Click();
        unLockBtn_Click();
        historyBtn_Click();



















        setActionBar("Home");
        checkEmail();

    }

    private void checkEmail() {
        if (!users.isEmailVerified()) {
            lockBtn.setEnabled(false);
            AlertDialog.Builder failedAlert = new AlertDialog.Builder(HomeActivity.this);
            failedAlert.setTitle("Email address verification failed");
            failedAlert.setMessage("Your email address haven't verified, please verify your email! ");
            failedAlert.setPositiveButton("Go to verify email",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
            failedAlert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (!users.isEmailVerified()) {
                                mAuth.signOut();
                                clearAllPre();
                                Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
            failedAlert.show();
        } else {
            clearAllPre();
            lockBtn.setEnabled(true);
        }
    }

    private void selectedItem() {

        choose_Door.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                SelectedPositionItem newSelected = new SelectedPositionItem();
                newSelected.setSelected(position);

                String item = choose_Door.getItemAtPosition(newSelected.getSelected()).toString();


                if(adapterView.getItemAtPosition(position).equals(item)){
                    chooseCurrentData_Firebase(item);
                }





            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


//    private void retrieveCurrentData_Firebase() {
//        load_data_from_setup();
//        mDataRef = database.getReference("/Users Details/"+ users.getUid());
//
//        mDataRef.orderByChild("uid").equalTo(users.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot data: dataSnapshot.getChildren()){
//                    setDBSharedPref(data.child("username").getValue().toString(), data.child("email").getValue().toString(), data.child("uid").getValue().toString(), (Boolean) data.child("lock_Status").getValue(), data.child("doorId").getValue().toString());
//                    load_data();
//                    update_UI();
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }

    private class SelectedPositionItem {

        private int spinnerValue;

        private int getSelected() {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int spinnerValue = sharedPref.getInt("spinner_item", -1);
            if (spinnerValue != -1) {
                // set the value of the spinner
                choose_Door.setSelection(spinnerValue, true);

            }
            return spinnerValue;
        }

        private void setSelected(int spinner_value) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = sharedPref.edit();
            editor.putInt("spinner_item", spinner_value);
            editor.apply();
            this.spinnerValue = spinner_value;
        }

    }

    private void sendNotificationSMS(String messageContent) {
        retrieveUser_Firebase();

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(
                "https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/SMS/Messages");
        String base64EncodedCredentials = "Basic "
                + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(),
                Base64.NO_WRAP);

        httppost.setHeader("Authorization",
                base64EncodedCredentials);
        try {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("From",
                    "+17605469479"));
            nameValuePairs.add(new BasicNameValuePair("To",
                    phone_db));
            nameValuePairs.add(new BasicNameValuePair("Body",
                    messageContent));

            httppost.setEntity(new UrlEncodedFormEntity(
                    nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            System.out.println("Entity post is: "
                    + EntityUtils.toString(entity));


        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }
    }

    private void chooseCurrentData_Firebase(String device) {
//        load_data_from_setup();

        mDataRef = database.getReference("/Users Details/"+ users.getUid() + "/Devices");

        mDataRef.orderByChild("deviceName").equalTo(device).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    setDeviceDBSharedPref(data.child("deviceName").getValue().toString(),(Boolean) data.child("lock_Status").getValue(),data.child("doorId").getValue().toString());
//                    setDBSharedPref(data.child("deviceName").getValue().toString(), data.child("username").getValue().toString(), data.child("email").getValue().toString(), data.child("uid").getValue().toString(), (Boolean) data.child("lock_Status").getValue(), data.child("doorId").getValue().toString());
                    load_data();


                }
                update_UI();


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void retrieveAllDoor_Firebase() {

        mDataRef = database.getReference("Users Details/" + users.getUid() + "/Devices");

        listener = mDataRef.orderByChild("deviceName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(spinnerDataList.size() <= 2){
                    for(DataSnapshot item : snapshot.getChildren()){
                        spinnerDataList.add(item.child("deviceName").getValue().toString());
                    }
                    adpater.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveUser_Firebase() {

        mDataRef = database.getReference("Users Details/" + users.getUid());

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setUsersDBSharedPref(snapshot.child("username").getValue().toString(), snapshot.child("email").getValue().toString(),snapshot.child("phone").getValue().toString(), snapshot.child("uid").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setUsersDBSharedPref(String username, String email, String phone, String uid) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        editor.putString("username_db",username);
        editor.putString("email_db",email);
        editor.putString("phone_num_db",phone);
        editor.putString("uid_db",uid);
        editor.apply();

    }

    private void setDeviceDBSharedPref(String device_name, Boolean lockStatus, String doorLock) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        editor.putString("device_name_db",device_name);
        editor.putBoolean("lockStatus_db",lockStatus);
        editor.putString("doorCode_db",doorLock);
        editor.apply();

    }

    private void load_data() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        device_name_db = sharedPref.getString("device_name_db", "");
        username_db = sharedPref.getString("username_db", "");
        email_db = sharedPref.getString("email_db", "");
        phone_db = sharedPref.getString("phone_num_db", "");
        uid_db = sharedPref.getString("uid_db", "");
        lock_status_db = sharedPref.getBoolean("lockStatus_db", false);
        door_lock_db = sharedPref.getString("doorCode_db", "");

    }

    private void load_data_from_setup() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        device_name_shared = sharedPref.getString("device_name", "");
        username_shared = sharedPref.getString("username", "");
        email_shared = sharedPref.getString("email", "");
        phone_shared = sharedPref.getString("phone_num", "");
        uid_shared = sharedPref.getString("uid", "");
        lock_status_shared = sharedPref.getBoolean("lock_status", false);
        door_lock_shared = sharedPref.getString("door_code", "");
    }


    private void clearAllPre(){

        sharedPref = getSharedPreferences("device_name_db", MODE_PRIVATE);
        sharedPref.edit().remove("device_name_db").commit();
        sharedPref = getSharedPreferences("phone_num_db", MODE_PRIVATE);
        sharedPref.edit().remove("phone_num_db").commit();
        sharedPref = getSharedPreferences("username_db", MODE_PRIVATE);
        sharedPref.edit().remove("username_db").commit();
        sharedPref = getSharedPreferences("email_db", MODE_PRIVATE);
        sharedPref.edit().remove("email_db").commit();
        sharedPref = getSharedPreferences("uid_db", MODE_PRIVATE);
        sharedPref.edit().remove("uid_db").commit();
        sharedPref = getSharedPreferences("lockStatus_db", MODE_PRIVATE);
        sharedPref.edit().remove("lockStatus_db").commit();
        sharedPref = getSharedPreferences("doorCode_db", MODE_PRIVATE);
        sharedPref.edit().remove("doorCode_db").commit();
    }

    private void setActionBar(String heading) {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
        actionBar.setTitle(heading);
        actionBar.show();

    }

    private void firebaseHistoryDatabaseRecord(String device_name,String description, String door_id, String lock_status, String dateAndTime) {
        load_data();

        HistoryInfo newHistory = new HistoryInfo(device_name, description, door_id,lock_status, dateAndTime);

        String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";

        mDataRef = database.getReference("/Users Details/");
        mDataRef.child(users.getUid()).child("Attempt History").child(device_name_db).child(getDateAndTime).setValue(newHistory);
    }

    private void update_UI(){

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(lock_status_db){
                    doorResultLabel.setTextColor(Color.GREEN);
                    doorResultLabel.setText("Unlock");
                    unlockBtn.setEnabled(false);
                    unlockBtn.setBackgroundColor(Color.LTGRAY);
                    lockBtn.setEnabled(true);
                    lockBtn.setBackgroundColor(Color.WHITE);
                }else{
                    doorResultLabel.setTextColor(Color.RED);
                    doorResultLabel.setText("Lock");
                    lockBtn.setEnabled(false);
                    lockBtn.setBackgroundColor(Color.LTGRAY);
                    unlockBtn.setEnabled(true);
                    unlockBtn.setBackgroundColor(Color.WHITE);

                }
            }
        });

        load_data();

        welcomeLabel.setText("Welcome Back ! " + username_db);
        doorCodeLabel.setText(door_lock_db);

    }

    private void lockBtn_Click(){

        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (users.isEmailVerified()) {
//                    sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
//                    sharedPref.edit().putBoolean("lockStatus_db", true).apply();
                    AlertDialog.Builder lockAlert = new AlertDialog.Builder(HomeActivity.this);
                    lockAlert.setTitle("Lock Door");
                    lockAlert.setMessage("Are you sure need to lock the door?");
                    lockAlert.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if(spinnerDataList != null){
                                        spinnerDataList.clear();
                                        adpater.notifyDataSetChanged();
                                    }

                                    mDataRef = database.getReference("/Users Details");
                                    mDataRef.child(users.getUid()).child("Devices").child(device_name_db).child("lock_Status").setValue((boolean)false);
                                    notification(device_name_db + " is lock");
                                    doorResultLabel.setTextColor(Color.RED);
                                    doorResultLabel.setText("Lock");
                                    sendNotificationSMS("\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is LOCK" + ", \nDOOR STATUS: LOCK");
                                    String lockDescri = device_name_db + " is LOCK at " + GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    firebaseHistoryDatabaseRecord(device_name_db, lockDescri, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());


                                    unlockBtn.setEnabled(true);
                                    unlockBtn.setBackgroundColor(Color.WHITE);

//                                    Intent reload = new Intent(HomeActivity.this, HomeActivity.class);
//                                    reload.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                                    startActivity(reload);
                                }
                            });
                    lockAlert.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    lockAlert.show();
                } else {
                    checkEmail();
                }
            }
        });
    }

    private void unLockBtn_Click(){

        unlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (users.isEmailVerified()) {
                    AlertDialog.Builder unlockAlert = new AlertDialog.Builder(HomeActivity.this);
                    unlockAlert.setTitle("Unlock Door");
                    unlockAlert.setMessage("Are you sure need to unlock the door?");
                    unlockAlert.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(spinnerDataList != null) {
                                        spinnerDataList.clear();
                                        adpater.notifyDataSetChanged();
                                    }

                                    mDataRef = database.getReference("/Users Details");
                                    mDataRef.child(users.getUid()).child("Devices").child(device_name_db).child("lock_Status").setValue((boolean) true);
                                    notification(device_name_db + " is unlock");
                                    doorResultLabel.setTextColor(Color.GREEN);
                                    doorResultLabel.setText("Unlock");
                                    sendNotificationSMS("\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is UNLOCK" + ", \nDOOR STATUS: UNLOCK");
                                    String unlockDescri = device_name_db + " is UNLOCK at " + GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    firebaseHistoryDatabaseRecord(device_name_db, unlockDescri, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());

                                    lockBtn.setEnabled(true);
                                    lockBtn.setBackgroundColor(Color.WHITE);
                                    unlockBtn.setEnabled(false);
                                    unlockBtn.setBackgroundColor(Color.LTGRAY);




                                    //                                Intent reload = new Intent(HomeActivity.this, HomeActivity.class);
                                    //                                reload.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    //                                startActivity(reload);
                                }
                            });
                    unlockAlert.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    unlockAlert.show();
                }
            }
        });


    }

    private void historyBtn_Click(){
        load_data();

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);

            }
        });

    }







    @Override
    protected void onResume() {
        super.onResume();
    }

    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    private String GetCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
        Date newDate = new Date();

        return dateFormat.format(newDate);
    }

    private String GetCurrentTime(){
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        Date newTime = new Date();

        return timeFormat.format(newTime);

    }


    public class UserDetails {

        public String deviceName;
        public String username;
        public String uid;
        public String email;
        public String password;
        public String doorId;
        public boolean lock_Status;

        public UserDetails() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public UserDetails(String uid, String device_name, String door_id, String username, String email, String password, Boolean lockStatus) {
            this.uid = uid;
            this.deviceName = device_name;
            this.doorId = door_id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.lock_Status = lockStatus;
        }
    }

    public class HistoryInfo {

        public String deviceName;
        public String doorId;
        public String lock_Status;
        public String dateAndTime;
        public String description;

        public HistoryInfo() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public HistoryInfo(String device_name, String description, String door_id, String lockStatus, String dateAndTime) {
            this.description = description;
            this.deviceName = device_name;
            this.doorId = door_id;
            this.lock_Status = lockStatus;
            this.dateAndTime = dateAndTime;
        }
    }


    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_btn) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);

//            mAuth.getInstance().signOut();
//            clearAllPre();
//            Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
//            startActivity(intent);
//            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        System.out.println(System.currentTimeMillis());
        if (!users.isEmailVerified()) {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            if (System.currentTimeMillis() - exitTime < 2000) {
                finish();
            } else {
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_LONG).show();
                exitTime = System.currentTimeMillis();
            }
        }

    }

}