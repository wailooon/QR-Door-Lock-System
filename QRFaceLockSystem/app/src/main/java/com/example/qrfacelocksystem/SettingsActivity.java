package com.example.qrfacelocksystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
//        setupSharedPreferences();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean("keyname",true);
        editor.putString("account_name","testing");
        sharedPref.getString("account_name","");
//        editor.putInt("keyname","int value");
//        editor.putFloat("keyname","float value");
//        editor.putLong("keyname","long value");
        editor.commit();




    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private FirebaseAuth mAuth;
        private FirebaseUser users;

        private FirebaseDatabase database;
        private DatabaseReference mDataRef;

        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;

        private String uid_db, username_db, email_db, password_db, door_lock_db;
        private boolean lock_status_db;

//        private Preference pref;
//        private String summaryStr;
//        String prefixStr;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            database = FirebaseDatabase.getInstance();

            mAuth = FirebaseAuth.getInstance();
            users = mAuth.getCurrentUser();

            retrieveCurrentData_Firebase();

//            // Get the custom preference
            Preference signOutBtn = (Preference) findPreference("sign_out_btn");
            signOutBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    mAuth.getInstance().signOut();
//              clearAllPre();
                    Intent intent = new Intent(getContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
//                  finish();
//                    SharedPreferences customSharedPreference = getSharedPreferences(
//                            "myCustomSharedPrefs", Activity.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = customSharedPreference.edit();
//                    editor.putString("myCustomPref", "The preference has been clicked");
//                    editor.commit();
                    return true;
                }
            });

            Preference addNewBtn = (Preference) findPreference("add_new_btn");
            addNewBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
//              clearAllPre();
                    Intent intent = new Intent(getContext(), ScanQRActivity.class);
                    startActivity(intent);
//                  finish();
//                    SharedPreferences customSharedPreference = getSharedPreferences(
//                            "myCustomSharedPrefs", Activity.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = customSharedPreference.edit();
//                    editor.putString("myCustomPref", "The preference has been clicked");
//                    editor.commit();
                    return true;
                }
            });


//            editUserName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//
//
//                    return true;
//                }
//            });

            // set texts correctly

        }

//        @Override
//        public void onResume() {
//            super.onResume();
//
//            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//
//            EditTextPreference editUserName = (EditTextPreference) findPreference("user_name");
//            editUserName.setSummary(sharedPref.getString("username_db",""));
//        }

        private void retrieveCurrentData_Firebase() {
            mDataRef = database.getReference("/Users Details");

            mDataRef.orderByChild("uid").equalTo(users.getUid().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        setDBSharedPref(data.child("username").getValue().toString(), data.child("email").getValue().toString(), data.child("uid").getValue().toString(), (Boolean) data.child("lock_Status").getValue(), data.child("doorId").getValue().toString());
                        load_data();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private void setDBSharedPref(String username, String email, String uid, Boolean lockStatus, String doorLock) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            editor = sharedPref.edit();
            editor.putString("username_db",username);
            editor.putString("email_db",email);
            editor.putString("uid_db",uid);
            editor.putBoolean("lockStatus_db",lockStatus);
            editor.putString("doorCode_db",doorLock);
            editor.apply();

        }

        private void load_data() {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            username_db = sharedPref.getString("username_db", "");
            email_db = sharedPref.getString("email_db", "");
            uid_db = sharedPref.getString("uid_db", "");
            lock_status_db = sharedPref.getBoolean("lockStatus_db", false);
            door_lock_db = sharedPref.getString("doorCode_db", "");

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