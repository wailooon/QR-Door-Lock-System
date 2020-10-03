package com.example.qrfacelocksystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        setActionBar("Settings");


    }



    public static class SettingsFragment extends PreferenceFragmentCompat {

        private FirebaseAuth mAuth;
        private FirebaseUser users;

        private FirebaseDatabase database;
        private DatabaseReference mDataRef;

        private List<String> deviceDataList = new ArrayList<>();

        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;

        private String uid_db, username_db, email_db, password_db, door_lock_db, phone_db;
        private boolean lock_status_db;

//        private Preference pref;
//        private String summaryStr;
//        String prefixStr;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            database = FirebaseDatabase.getInstance();

            mAuth = FirebaseAuth.getInstance();
            users = mAuth.getCurrentUser();

            retrieveCurrentData_Firebase();
            load_data();


            setPreferencesFromResource(R.xml.root_preferences, rootKey);


            addNewDevice();
            deleteDevice();

            updateUsername();
            showEmail();
            updatePassword();
            updatePhoneNumber();
            showUid();

            signOut();




//            Preference historyBtn = (Preference) findPreference("history_btn");
//            historyBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//
//                public boolean onPreferenceClick(Preference preference) {
//                    Intent intent = new Intent(getContext(), HistoryActivity.class);
//                    startActivity(intent);
//
//                    return true;
//                }
//            });


        }

        private void addNewDevice() {
            Preference addNewBtn = (Preference) findPreference("add_new_btn");
            addNewBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), ScanQRActivity.class);
                    startActivity(intent);
//                  finish();
                    return true;
                }
            });
        }

        private void deleteDevice() {
            Preference deleteBtn = (Preference) findPreference("delete_btn");

            mDataRef = database.getReference("Users Details/" + users.getUid() + "/Devices");

            mDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    deviceDataList.clear();
                    for(DataSnapshot item : snapshot.getChildren()){
                        deviceDataList.add(item.child("deviceName").getValue().toString());
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            deleteBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    if(deviceDataList.size() <= 1) {
                        notification("You can't remove last device");
                    }else{
                        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Choose a device you want to delete");
                        deleteDialog.setSingleChoiceItems(deviceDataList.toArray(new String[deviceDataList.size()]), -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDataRef = database.getReference("Users Details/" + users.getUid() + "/Devices/");

                                mDataRef.orderByChild("deviceName").equalTo(String.valueOf(deviceDataList.get(i))).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot item : snapshot.getChildren()) {
                                            item.getRef().removeValue();

                                            mDataRef = database.getReference("Users Details/" + users.getUid() + "/Attempt History/" + item.child("deviceName").getValue().toString());

                                            mDataRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    snapshot.getRef().removeValue();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            mDataRef = database.getReference("IsUsedQRCode/" + item.child("doorId").getValue().toString() );

                                            mDataRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    snapshot.getRef().removeValue();

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });



                                notification(String.valueOf(deviceDataList.get(i)) + " is successful delete!");
                                deviceDataList.clear();
                                dialogInterface.dismiss();
                            }
                        });

                        deleteDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        AlertDialog dialog = deleteDialog.create();
                        dialog.show();
                    }

                    return true;
                }
            });
        }

        private void getDeviceData(String deviceName) {


        }


        private void updateUsername() {
            EditTextPreference user_name = (EditTextPreference) findPreference("user_name_editText");

            user_name.setDialogTitle("New Username");
            user_name.setSummary(username_db);
            user_name.setText(username_db);

            user_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification("Change username successful!");
                    mDataRef = database.getReference("/Users Details");
                    mDataRef.child(users.getUid()).child("username").setValue(newValue.toString());
                    retrieveCurrentData_Firebase();
                    preference.setSummary(newValue.toString());

                    return true;
                }
            });
        }

        private void updatePassword() {
            EditTextPreference password = (EditTextPreference) findPreference("password_editText");

            password.setText("");
            password.setDialogTitle("New Password");

            password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if(newValue.toString().length() < 8){
                            notification("Password must between 8 to 16 character");
                            return false;
                        }else{
                            users.updatePassword(newValue.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    notification("Change password successful!");
                                }
                            });
                            mDataRef = database.getReference("/Users Details");
                            mDataRef.child(users.getUid()).child("password").setValue(newValue.toString());
                            retrieveCurrentData_Firebase();

                            return true;
                        }
                }
            });
        }

        private void updatePhoneNumber() {
            final EditTextPreference phone = (EditTextPreference) findPreference("phone_editText");

            phone.setDialogTitle("New Phone Number");
            phone.setSummary(phone_db);

            phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if(isValidMobile(newValue.toString())){
                        if(newValue.toString().contains("+")){
                            notification("Change phone number successful!");
                            mDataRef = database.getReference("/Users Details");
                            mDataRef.child(users.getUid()).child("phone").setValue(newValue.toString());
                            preference.setSummary(newValue.toString());
                            retrieveCurrentData_Firebase();
                            return true;

                        }else{
                            notification("Phone number must start with country code! (Example: +61)");
                            return false;
                        }
                    }else{
                        notification("Invalid phone number format!");
                        return false;
                    }

                }
            });
        }

        private void showEmail() {
            Preference email = (Preference) findPreference("email_editText");

            email.setSummary(email_db);
            email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    notification("You can't change email address!");
                    return true;
                }
            });
        }

        private void showUid() {
            Preference uid = (Preference) findPreference("uid_editText");

            uid.setSummary(uid_db);
            uid.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    notification("You can't change user uid!");
                    return true;
                }
            });
        }

        private void signOut() {
            Preference signOutBtn = (Preference) findPreference("sign_out_btn");
            signOutBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    mAuth.getInstance().signOut();
                    clearAllPre();
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });
        }


        private void retrieveCurrentData_Firebase() {
            mDataRef = database.getReference("/Users Details/" + users.getUid());

            mDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setUsersDBSharedPref(dataSnapshot.child("username").getValue().toString(), dataSnapshot.child("email").getValue().toString(), dataSnapshot.child("uid").getValue().toString(), dataSnapshot.child("phone").getValue().toString());

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private void setUsersDBSharedPref(String username, String email, String uid, String phone) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            editor = sharedPref.edit();
            editor.putString("username_db",username);
            editor.putString("email_db",email);
            editor.putString("uid_db",uid);
            editor.putString("phone_db",phone);
            editor.apply();

        }

        private void setDeviceDBSharedPref(String device_name, Boolean lockStatus, String doorLock, String phone) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            editor = sharedPref.edit();
            editor.putString("device_name_db",device_name);
            editor.putBoolean("lockStatus_db",lockStatus);
            editor.putString("doorCode_db",doorLock);
            editor.putString("phone_db", phone);
            editor.apply();

        }

        private void load_data() {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            username_db = sharedPref.getString("username_db", "");
            email_db = sharedPref.getString("email_db", "");
            password_db = sharedPref.getString("password_db", "");
            phone_db = sharedPref.getString("phone_db", "");
            uid_db = sharedPref.getString("uid_db", "");
            lock_status_db = sharedPref.getBoolean("lockStatus_db", false);
            door_lock_db = sharedPref.getString("doorCode_db", "");

        }

        private void clearAllPre() {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPref.edit().remove("device_name_db").commit();
            sharedPref.edit().remove("phone_num_db").commit();
            sharedPref.edit().remove("phone_db").commit();
            sharedPref.edit().remove("username_db").commit();
            sharedPref.edit().remove("email_db").commit();
            sharedPref.edit().remove("uid_db").commit();
            sharedPref.edit().remove("password_db").commit();
            sharedPref.edit().remove("lockStatus_db").commit();
            sharedPref.edit().remove("doorCode_db").commit();
        }

        private void notification(String message) {
            Toast.makeText(getContext(), message,
                    Toast.LENGTH_SHORT).show();
        }

        private boolean isValidMobile(String phone) {
            if(!Pattern.matches("[a-zA-Z]+", phone)) {
                return phone.length() > 6 && phone.length() <= 13;
            }
            return false;
        }


    }


    private void setActionBar(String heading) {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
        actionBar.setTitle(heading);
        actionBar.show();

    }




//    private void setupSharedPreferences() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
//    }

//    // Method to set Visibility of Text.
//    private void setTextVisible(boolean display_text) {
//        if (display_text == true) {
//            visibletxt.setVisibility(View.VISIBLE);
//        } else {
//            visibletxt.setVisibility(View.INVISIBLE);
//        }
//    }



}