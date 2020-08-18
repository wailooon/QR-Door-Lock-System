package com.example.qrfacelocksystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

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
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        setActionBar("Settings");
//        setupSharedPreferences();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean("keyname",true);
        editor.putString("account_name","testing");
//        sharedPref.getString("account_name",);
//        editor.putInt("keyname","int value");
//        editor.putFloat("keyname","float value");
//        editor.putLong("keyname","long value");
        editor.commit();

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


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
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