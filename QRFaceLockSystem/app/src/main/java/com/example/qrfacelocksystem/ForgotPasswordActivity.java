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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private EditText emailFieldBox;
    private Button resetPwBtn;
    private ProgressBar progressBar;

    private ActionBar actionBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailFieldBox = (EditText) findViewById(R.id.emailTextField);

        resetPwBtn = (Button) findViewById(R.id.restPwButton);

        progressBar = (ProgressBar) findViewById(R.id.activity_reset_progressBar);

        progressBar.setVisibility(View.INVISIBLE);   //set invisibility

        mAuth = FirebaseAuth.getInstance();

        setActionBar("Reset Password");

        resetPwBtn_Click();
    }

    private void resetPwBtn_Click() {
        email = emailFieldBox.getText().toString();

        resetPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail()) {
                    progressBar.setVisibility(view.VISIBLE);   //set visibility

                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // password reset email send success, update UI with the signed-in user's information
                                Log.d(TAG, "resetPasswordEmail:success");
                                alertBox("Password reset email has been sent! Please check your mailbox.");
                                Intent intent = new Intent (ForgotPasswordActivity.this, SignInActivity.class);
                                startActivity(intent);
//                            firebaseDatabaseRecord(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString());
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                            }else {
                                // If password reset email send fails, display a message to the user.
                                Log.w(TAG, "resetPasswordEmail:failure", task.getException());
                                AlertDialog.Builder alert1 = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                alert1.setTitle("Reset Password Failed");
                                alert1.setMessage("Send reset password email failed, Please try again!");
                                alert1.setPositiveButton("Try Again",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                emailFieldBox.setText("");
                                                resetPwBtn_Click();
                                            }
                                        });
                                alert1.show();
//                            updateUI(null);
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private boolean validateEmail(){

        email = emailFieldBox.getText().toString();

        if(email.isEmpty()){
            emailFieldBox.setError("Please enter your email address!");
            return false;
        }else if(!Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(email).matches()){
            emailFieldBox.setError("Please enter a valid email address");
            return false;
        }else{
            emailFieldBox.setError(null);
            return true;
        }
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


    private void alertBox(String message) {
        Toast.makeText(ForgotPasswordActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}