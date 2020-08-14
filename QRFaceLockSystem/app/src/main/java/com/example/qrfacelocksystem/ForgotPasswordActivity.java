package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private EditText emailField;
    private Button resetPwBtn;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailField = (EditText) findViewById(R.id.emailTextField);
        emailField = (EditText) findViewById(R.id.emailTextField);

        resetPwBtn = (Button) findViewById(R.id.restPwButton);

        setActionBar("Reset Password");

        resetPwBtn_Click();
    }

    private void resetPwBtn_Click() {
        resetPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(emailField.equals("")){
                    firebaseResetPassword(emailField.getText().toString());
                }else{
                    notification("Please enter your email address!");
                }
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

    private void firebaseResetPassword(String email_address) {

        mAuth = FirebaseAuth.getInstance();

        mAuth.sendPasswordResetEmail(email_address).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // password reset email send success, update UI with the signed-in user's information
                    Log.d(TAG, "resetPasswordEmail:success");
                    notification("Password reset email has been sent! Please check your mailbox.");
                    Intent intent = new Intent (ForgotPasswordActivity.this, SignInActivity.class);
                    startActivity(intent);
//                            firebaseDatabaseRecord(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString());
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                } else {
                    // If password reset email send fails, display a message to the user.
                    Log.w(TAG, "resetPasswordEmail:failure", task.getException());
                    AlertDialog.Builder alert1 = new AlertDialog.Builder(ForgotPasswordActivity.this);
                    alert1.setTitle("Reset Password Failed");
                    alert1.setMessage("Sending password reset email failed, Please try again!");
                    alert1.setPositiveButton("Try Again",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    resetPwBtn_Click();
                                }
                            });
                    alert1.show();
//                            updateUI(null);
                }
            }
        });

    }


    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }
}