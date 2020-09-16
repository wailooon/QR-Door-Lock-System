package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;


public class NewSetupActivity extends AppCompatActivity {

    private static final String TAG = "NewSetup";
    private FirebaseAuth mAuth;
    private FirebaseUser users;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private TextView doorLock_Code;
    private EditText usernameField, emailField, passwordField, deviceNameField, phoneField;
    private Button setupButton;
    private CheckBox showPasswordCheckbox;
    private ProgressBar progressBar;

    private ActionBar actionBar;

    private String confirmation_message;
    private Boolean lock_Status = false;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsetup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = mAuth.getCurrentUser();


        doorLock_Code = (TextView) findViewById(R.id.door_code);

        usernameField = (EditText) findViewById(R.id.nameTextField);
        emailField = (EditText) findViewById(R.id.emailTextField);
        passwordField = (EditText) findViewById(R.id.passwordTextField);
        deviceNameField = (EditText) findViewById(R.id.deviceNameTextField);
        phoneField = (EditText)findViewById(R.id.phoneTextField);

        setupButton = (Button) findViewById(R.id.addNewButton);

        showPasswordCheckbox = (CheckBox) findViewById(R.id.showPassCheckBox);

        progressBar = (ProgressBar) findViewById(R.id.activity_setup_progressBar);

        progressBar.setVisibility(View.INVISIBLE);   //set invisibility



//        doorLock_Code.setText(GenerateRandomString.randomString(8));

        setActionBar("New Device Setup");

        getDoorCode();
        showPasswordCheckbox();
        setupBtn_Click();


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
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

    private void getDoorCode(){
        Intent doorCode = getIntent();
        Bundle door_code = doorCode.getExtras();

        if(door_code!=null)
        {
            String code =(String) door_code.get("DoorCode");
            doorLock_Code.setText(code);
        }
    }


    private void firebaseDatabaseRecord(String device_name, String door_id, String user_name, String email_address, String phone_number, String password_field, Boolean lock_status) {
        UserDetails newUser = new UserDetails(users.getUid(), user_name, email_address, phone_number,password_field);
        DeviceDetails newDevice = new DeviceDetails(door_id,device_name,lock_status);

        mDataRef = database.getReference("/Users Details");
        mDataRef.child(users.getUid()).setValue(newUser);

        mDataRef = database.getReference("/Users Details");
        mDataRef.child(users.getUid()).child("Devices").child(device_name).setValue(newDevice);

        mDataRef = database.getReference("/IsUsedQRCode/");
        mDataRef.child(door_id).child("doorId").setValue(door_id);

//        mDataRef = database.getReference("/Users Details/" + users.getUid() + "/" + "Devices");
//        mDataRef.child(doorLock_Code.getText().toString()).setValue(newDevice);
    }


    private void setupBtn_Click() {
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail() | validatePassword()) {
                    mAuth.fetchSignInMethodsForEmail(emailField.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                                    boolean check = !task.getResult().getSignInMethods().isEmpty();

                                    if (!check) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        mAuth.createUserWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString()).addOnCompleteListener(NewSetupActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d(TAG, "createUserWithEmail:success");

                                                    users = FirebaseAuth.getInstance().getCurrentUser();
                                                    users.sendEmailVerification().addOnCompleteListener(NewSetupActivity.this, new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            firebaseDatabaseRecord(deviceNameField.getText().toString(), doorLock_Code.getText().toString(), usernameField.getText().toString(), emailField.getText().toString(),phoneField.getText().toString(), passwordField.getText().toString(), lock_Status);

                                                            confirmation_message = "A confirmation email has been sent to " + emailField.getText().toString().trim() + ". Please check your mailbox and activate your account.";
                                                            sharedPref = PreferenceManager.getDefaultSharedPreferences(NewSetupActivity.this);
                                                            editor = sharedPref.edit();
                                                            editor.putString("ConfirmationMessage", confirmation_message);

                                                            editor.putString("username", usernameField.getText().toString());
                                                            editor.putString("password", passwordField.getText().toString());
                                                            editor.putString("device_name", deviceNameField.getText().toString());
                                                            editor.putString("door_code", doorLock_Code.getText().toString());
                                                            editor.putString("phone_num", phoneField.getText().toString());
                                                            editor.putString("email", emailField.getText().toString());
                                                            editor.putBoolean("lock_status", lock_Status);
                                                            editor.apply();


//                                                             updateUI(user);
                                                            Intent intent = new Intent(NewSetupActivity.this, ConfimationEmailActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            usernameField.setText("");
                                                            emailField.setText("");
                                                            passwordField.setText("");
                                                            mAuth.signOut();
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });

                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                    AlertDialog.Builder failedAlert = new AlertDialog.Builder(NewSetupActivity.this);
                                                    failedAlert.setTitle("Setup Failed");
                                                    failedAlert.setMessage("Setup failed, Please try again!");
                                                    failedAlert.setPositiveButton("Try again",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    emailField.setText("");
                                                                    passwordField.setFocusable(true);
                                                                }
                                                            });
                                                    failedAlert.show();
//                                                          updateUI(null);
                                                }
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });

                                    } else {
                                        emailField.setError("Email address already been used!");
                                    }
                                }
                            });
                }
            }
        });

    }

    private void showPasswordCheckbox() {
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


    private class completionListener implements DatabaseReference.CompletionListener {

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            if (databaseError != null) {
                notification(databaseError.getMessage());
            }
        }
    }


    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }


//    public void diaglogBox(String message, String title, int buttonNum, String buttonLabel1,String buttonLabel2) {
//        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//        builder1.setTitle(title);
//        builder1.setMessage(message);
//
//        switch (buttonNum) {
//            case 1:
//                builder1.setPositiveButton(buttonLabel1,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                break;
//            case 2:
//                builder1.setNegativeButton(buttonLabel1,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                break;
//            case 3:
//                builder1.setPositiveButton(buttonLabel1,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                builder1.setNegativeButton(buttonLabel2,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                break;
//        }
//            AlertDialog alertDialog = builder1.create();
//            alertDialog.show();
//        }

    private boolean validateEmail() {

        String emailInput = emailField.getText().toString().trim();

        if (emailInput.isEmpty()) {
            emailField.setError("Field can't be empty!");
            return false;
        } else if (!Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(emailInput).matches()) {
            emailField.setError("Please enter a valid email address");
            return false;
        } else {
            emailField.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = passwordField.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            passwordField.setError("Field can't be empty!");
            return false;
        } else if (!Pattern.compile("^.{8,16}$").matcher(passwordInput).matches()) {
            passwordField.setError("Password must between 8 to 16 character");
            return false;
        } else {
            passwordField.setError(null);
            return true;
        }
    }

    public class UserDetails {

        public String username;
        public String uid;
        public String email;
        public String phone;
        public String password;

        public UserDetails() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public UserDetails(String uid, String username, String email,String phone, String password) {
            this.uid = uid;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.password = password;
        }
    }

    public class DeviceDetails {
        public String deviceName;
        public String doorId;
        public boolean lock_Status;

        public DeviceDetails() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public DeviceDetails(String door_id, String device_name, Boolean lockStatus) {

            this.doorId = door_id;
            this.deviceName = device_name;
            this.lock_Status = lockStatus;
        }
    }

//    public static class GenerateRandomString {
//
//        public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        public static Random RANDOM = new Random();
//
//        public static String randomString(int len) {
//            StringBuilder sb = new StringBuilder(len);
//
//            for (int i = 0; i < len; i++) {
//                sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
//            }
//
//            return sb.toString();
//        }
//
//    }

//    database = FirebaseDatabase.getInstance();
//    mDataRef = database.getReference("/Users Details/Door Lock ID/");

}