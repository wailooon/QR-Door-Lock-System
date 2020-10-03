package com.example.qrfacelocksystem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfimationEmailActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    private FirebaseAuth mAuth;
    private FirebaseUser users;

    private TextView message_box;
    private Button doneButton;
    private String message_text;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confimation_email);

        mAuth = FirebaseAuth.getInstance();
        users = mAuth.getCurrentUser();

        message_box = (TextView) findViewById(R.id.messageBox);

        doneButton = (Button) findViewById(R.id.doneButton);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        message_text = sharedPref.getString("ConfirmationMessage", "");
        message_box.setText(message_text);

        setActionBar("Verify Email Address");

        doneButton_Click();
    }

    private void doneButton_Click(){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref = getSharedPreferences("ConfirmationMessage", MODE_PRIVATE);
                sharedPref.edit().remove("ConfirmationMessage").commit();

                Intent intent = new Intent(ConfimationEmailActivity.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
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
}