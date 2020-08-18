package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.StatusBarManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignIn";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private EditText emailField, passwordField;
    private Button signInBtn, newDeviceBtn, forgotPwBtn;
    private CheckBox showPasswordCheckbox;
    private ProgressBar progressBar;

    private String email,password;

    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        emailField = (EditText) findViewById(R.id.emailTextField);
        passwordField = (EditText) findViewById(R.id.passwordTextField);

        signInBtn = (Button) findViewById(R.id.signInButton);
        newDeviceBtn = (Button) findViewById(R.id.newDeviceButton);
        forgotPwBtn = (Button) findViewById(R.id.forgotPwButton);
        progressBar = (ProgressBar) findViewById(R.id.activity_sigin_progressBar);

        progressBar.setVisibility(View.INVISIBLE);   //set invisibility


        showPasswordCheckbox = (CheckBox)findViewById(R.id.showPassCheckBox);

        setActionBar("Sign In");

        signInBtn_Click();
        showPasswordCheckbox();
        newDevice_Click();
        forgotPw_Click();



    }


    private void showPasswordCheckbox(){
        showPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // show password
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // hide password
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    private void signInBtn_Click() {
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateEmail() && validatePassword()) {
                        progressBar.setVisibility(View.VISIBLE);

                        mAuth.signInWithEmailAndPassword(emailField.getText().toString(),passwordField.getText().toString()).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInUserWithEmail:success");
                                    AlertDialog.Builder successAlert = new AlertDialog.Builder(SignInActivity.this);
                                    successAlert.setTitle("Sign-in Success");
                                    successAlert.setMessage("Done sign in");
                                    successAlert.setPositiveButton("Done",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    emailField.setText(null);
                                                    passwordField.setText(null);
                                                    emailField.requestFocus();
                                                }
                                            });
                                    successAlert.show();
//                                  firebaseDatabaseRecord(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString());
//                                  FirebaseUser user = mAuth.getCurrentUser();
//                                  updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInUserWithEmail:failure", task.getException());
                                    AlertDialog.Builder failedAlert = new AlertDialog.Builder(SignInActivity.this);
                                    failedAlert.setTitle("Sign-in Failed");
                                    failedAlert.setMessage("Need new an account?");
                                    failedAlert.setPositiveButton("Create new an account",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(SignInActivity.this, NewSetupActivity.class);
                                                    startActivity(intent);
                                                    emailField.setText(null);
                                                    passwordField.setText(null);
                                                    emailField.requestFocus();
                                                }
                                            });
                                    failedAlert.setNegativeButton("Try again",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    passwordField.setText(null);
                                                    emailField.requestFocus();
                                                }
                                            });
                                    failedAlert.show();
//                                  updateUI(null);
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                }
            }
        });

    }

    private void newDevice_Click() {
        newDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (SignInActivity.this, ScanQRActivity.class);
                startActivity(intent);
                emailField.setText(null);
                passwordField.setText(null);

            }
        });

    }

    private void forgotPw_Click() {
        forgotPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                emailField.setText(null);
                passwordField.setText(null);

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


    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    private boolean validateEmail(){

        email = emailField.getText().toString().trim();

        if(email.isEmpty()){
            emailField.setError("Please enter your email address!");
            return false;
        }else if(!Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(email).matches()){
            emailField.setError("Please enter a valid email address");
            return false;
        }else{
            emailField.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        password = passwordField.getText().toString().trim();

        if(password.isEmpty()){
            passwordField.setError("Please enter your password!");
            return false;
        }else if(!Pattern.compile("^.{8,16}$").matcher(password).matches()){
            passwordField.setError("Please enter a valid email address! Password must between 8 to 16 character");
            return false;
        }else{
            passwordField.setError(null);
            return true;
        }
    }

}