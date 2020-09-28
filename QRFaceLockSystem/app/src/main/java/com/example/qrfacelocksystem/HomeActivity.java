package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private long exitTime = 0;

    private FirebaseAuth mAuth;
    private FirebaseUser users;

    private FusedLocationProviderClient fusedLocationProviderClient;

    public static final String ACCOUNT_SID = "ACfeb3175660e8f538c64caa1e9d82b912";
    public static final String AUTH_TOKEN = "b11d0f1c732e898fceace61770364bab";
    public static final String twillioPhone = "+17605469479";

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private String voiceText;

    private String username_shared, email_shared, uid_shared, door_lock_shared, device_name_shared, phone_shared;
    private Boolean lock_status_shared;

    private int currentDoorId;
    private String currentLocation;
    private String uid_db, username_db, email_db, password_db, door_lock_db, device_name_db, phone_db;
    private boolean lock_status_db;

    private ActionBar actionBar;

    private Spinner choose_Door;
    private ArrayAdapter<String> adpater;
    private ArrayList<String> spinnerDataList;
    private ValueEventListener listener;

    private Button unlockBtn, lockBtn, historyBtn;
    private Button logout;
    private ImageButton voiceBtn;

    private ImageView image_DoorStatus;

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

        voiceBtn = (ImageButton)findViewById(R.id.voice_activated_button);

        image_DoorStatus = (ImageView)findViewById(R.id.image_DoorStatus);

        welcomeLabel = (TextView)findViewById(R.id.welcomeSlogan);
        doorCodeLabel = (TextView)findViewById(R.id.doorID);
        doorResultLabel = (TextView)findViewById(R.id.doorStatusResult);

        load_data();
        retrieveUser_Firebase();

        selectedItem();

        retrieveAllDoor_Firebase();


        lockBtn_Click();
        unLockBtn_Click();
        historyBtn_Click();
        voiceBtn_Click();


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
                                users.sendEmailVerification().addOnCompleteListener(HomeActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        notification("Successful resend the verify email! ");
                                        mAuth.signOut();
                                        clearAllPre();
                                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
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

        String body = messageContent;
        String from = twillioPhone;
        String to = phone_db;

        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP
        );

        Map<String, String> data = new HashMap<>();
        data.put("From", from);
        data.put("To", to);
        data.put("Body", body);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twilio.com/2010-04-01/")
                .build();
        TwilioApi api = retrofit.create(TwilioApi.class);

        api.sendMessage(ACCOUNT_SID, base64EncodedCredentials, data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                    Log.d("TAG", "onResponse->success");
                else
                    Log.d("TAG", "onResponse->failure");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("TAG", "onFailure");
            }
        });

    }

    interface TwilioApi {
        @FormUrlEncoded
        @POST("Accounts/{ACCOUNT_SID}/Messages")
        Call<ResponseBody> sendMessage(
                @Path("ACCOUNT_SID") String accountSId,
                @Header("Authorization") String signature,
                @FieldMap Map<String, String> metadata
        );
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

        currentLocation = sharedPref.getString("location","");
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

    private void firebaseHistoryDatabaseRecord(String imageResource, String device_name,String description,String location, String door_id, String lock_status, String dateAndTime) {
        load_data();

        HistoryInfo newHistory = new HistoryInfo(imageResource, device_name, description,location, door_id,lock_status, dateAndTime);

        String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";

        mDataRef = database.getReference("/Users Details/");
        mDataRef.child(users.getUid()).child("Attempt History").child(device_name_db).child(getDateAndTime).setValue(newHistory);
    }


    private void GetCurrentLocation() {
        //Initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Check permission
        if(ActivityCompat.checkSelfPermission(HomeActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //When permission granted
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize Location
                    Location location = task.getResult();
                    if(location != null){
                        try {
                            //Initialize geoCoder
                            Geocoder geocoder = new Geocoder(HomeActivity.this,
                                    Locale.getDefault());

                            //Initialize address list
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);


                            setCurrentLocation(addresses.get(0).getAddressLine(0));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else{
            //When permission denied
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        }
    }

    private void setCurrentLocation(String addressLine) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        editor.putString("location",addressLine);
        editor.apply();
    }

    private void update_UI(){

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(lock_status_db){
                    image_DoorStatus.setImageResource(R.drawable.unlock_status);
                    doorResultLabel.setTextColor(Color.GREEN);
                    doorResultLabel.setText("Unlock");
                    unlockBtn.setEnabled(false);
                    unlockBtn.setBackgroundColor(Color.LTGRAY);
                    lockBtn.setEnabled(true);
                    lockBtn.setBackgroundColor(Color.WHITE);
                }else{
                    image_DoorStatus.setImageResource(R.drawable.lock_status);
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
                                    image_DoorStatus.setImageResource(R.drawable.lock_status);

                                    GetCurrentLocation();
                                    load_data();
                                    String lockDescri = device_name_db + " is LOCK at " + currentLocation ;
                                    String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    String lockVector = "https://firebasestorage.googleapis.com/v0/b/qrfacelocksystem.appspot.com/o/lock_icon.png?alt=media&token=c6043d21-cdec-4ec4-a10e-01448608ab42";
                                    String smsMessage = "\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is LOCK " + "\nLocation: " + currentLocation + ", \nDOOR STATUS: LOCK";
                                    firebaseHistoryDatabaseRecord(lockVector, device_name_db, lockDescri,currentLocation, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());
                                    sendNotificationSMS(smsMessage);


                                    unlockBtn.setEnabled(true);
                                    unlockBtn.setBackgroundColor(Color.WHITE);
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
                                    image_DoorStatus.setImageResource(R.drawable.unlock_status);


                                    GetCurrentLocation();
                                    load_data();
                                    String unlockDescri = device_name_db + " is UNLOCK at " + currentLocation ;
                                    String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                                    String unlockVector = "https://firebasestorage.googleapis.com/v0/b/qrfacelocksystem.appspot.com/o/unlock_icon.png?alt=media&token=4e488d6a-9515-4ee4-85f7-f912f037ce89";
                                    String smsMessage = "\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is UNLOCK " + "\nLocation: " + currentLocation + ", \nDOOR STATUS: UNLOCK";
                                    firebaseHistoryDatabaseRecord(unlockVector, device_name_db, unlockDescri, currentLocation, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());
                                    sendNotificationSMS(smsMessage);


                                    lockBtn.setEnabled(true);
                                    lockBtn.setBackgroundColor(Color.WHITE);
                                    unlockBtn.setEnabled(false);
                                    unlockBtn.setBackgroundColor(Color.LTGRAY);

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

    private void voiceBtn_Click(){
        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voice_activated();

            }
        });

    }

    private void voice_activated(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say lock / unlock the door");

        try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e){
            notification(e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case REQUEST_CODE_SPEECH_INPUT:
                if(resultCode == RESULT_OK && null != data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    voiceText = result.get(0);

                    if(voiceText.equals("lock the door")){
                        if(doorResultLabel.getText().equals("Lock")){
                            notification("Door is still Lock");
                        }else{
                            mDataRef = database.getReference("/Users Details");
                            mDataRef.child(users.getUid()).child("Devices").child(device_name_db).child("lock_Status").setValue((boolean)false);
                            notification(device_name_db + " is lock");
                            doorResultLabel.setTextColor(Color.RED);
                            doorResultLabel.setText("Lock");
                            image_DoorStatus.setImageResource(R.drawable.lock_status);

                            GetCurrentLocation();
                            load_data();
                            String lockDescri = device_name_db + " is LOCK at " + currentLocation ;
                            String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                            String lockVector = "https://firebasestorage.googleapis.com/v0/b/qrfacelocksystem.appspot.com/o/lock_icon.png?alt=media&token=c6043d21-cdec-4ec4-a10e-01448608ab42";
                            String smsMessage = "\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is LOCK " + "\nLocation: " + currentLocation + ", \nDOOR STATUS: LOCK";
                            firebaseHistoryDatabaseRecord(lockVector, device_name_db, lockDescri,currentLocation, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());
                            sendNotificationSMS(smsMessage);

                            unlockBtn.setEnabled(true);
                            unlockBtn.setBackgroundColor(Color.WHITE);
                            lockBtn.setEnabled(false);
                            lockBtn.setBackgroundColor(Color.LTGRAY);
                        }

                    }else if(voiceText.equals("unlock the door")){
                        if(doorResultLabel.getText().equals("Unlock")){
                            notification("Door is still Unlock");
                        }else{
                            mDataRef = database.getReference("/Users Details");
                            mDataRef.child(users.getUid()).child("Devices").child(device_name_db).child("lock_Status").setValue((boolean) true);
                            notification(device_name_db + " is unlock");
                            doorResultLabel.setTextColor(Color.GREEN);
                            doorResultLabel.setText("Unlock");
                            image_DoorStatus.setImageResource(R.drawable.unlock_status);

                            GetCurrentLocation();
                            load_data();
                            String unlockDescri = device_name_db + " is UNLOCK at " + currentLocation ;
                            String getDateAndTime = GetCurrentDate() + "(" + GetCurrentTime() + ")";
                            String unlockVector = "https://firebasestorage.googleapis.com/v0/b/qrfacelocksystem.appspot.com/o/unlock_icon.png?alt=media&token=4e488d6a-9515-4ee4-85f7-f912f037ce89";
                            String smsMessage = "\nHi, " + username_db + ", \nYour door " + device_name_db + "(" + door_lock_db + ")" + " in " + GetCurrentDate() + " (" + GetCurrentTime() +") is UNLOCK " + "\nLocation: " + currentLocation + ", \nDOOR STATUS: UNLOCK";
                            firebaseHistoryDatabaseRecord(unlockVector, device_name_db, unlockDescri, currentLocation, door_lock_db, doorResultLabel.getText().toString(),getDateAndTime.toString());
                            sendNotificationSMS(smsMessage);


                            lockBtn.setEnabled(true);
                            lockBtn.setBackgroundColor(Color.WHITE);
                            unlockBtn.setEnabled(false);
                            unlockBtn.setBackgroundColor(Color.LTGRAY);
                        }
                    }
                }
                break;
        }
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
        DateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
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

        public String imageResource;
        public String deviceName;
        public String doorId;
        public String lock_Status;
        public String dateAndTime;
        public String description;
        public String location;

        public HistoryInfo() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public HistoryInfo(String imageResource, String device_name, String description,String location, String door_id, String lockStatus, String dateAndTime) {
            this.imageResource = imageResource;
            this.location = location;
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