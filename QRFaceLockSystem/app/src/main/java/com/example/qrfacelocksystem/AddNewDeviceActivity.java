package com.example.qrfacelocksystem;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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


public class AddNewDeviceActivity extends AppCompatActivity {

    private static final String TAG = "NewSetup";
    private FirebaseAuth mAuth;
    private FirebaseUser users;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private TextView doorLock_Code;
    private EditText usernameField, emailField, passwordField, deviceNameField;
    private Button addButton;
    private CheckBox showPasswordCheckbox;
    private ProgressBar progressBar;

    private ActionBar actionBar;

    private String username_shared, email_shared, door_lock_shared, password_shared, device_name_shared;
    private Boolean lock_status_shared;

    private String confirmation_message;
    private Boolean lock_Status = false;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_device);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = mAuth.getCurrentUser();


        doorLock_Code = (TextView) findViewById(R.id.door_code);

//        usernameField = (EditText) findViewById(R.id.nameTextField);
//        emailField = (EditText) findViewById(R.id.emailTextField);
//        passwordField = (EditText) findViewById(R.id.passwordTextField);
        deviceNameField = (EditText) findViewById(R.id.deviceNameField);

        addButton = (Button) findViewById(R.id.addNewButton);

        progressBar = (ProgressBar) findViewById(R.id.activity_setup_progressBar);

        progressBar.setVisibility(View.INVISIBLE);   //set invisibility



//        doorLock_Code.setText(GenerateRandomString.randomString(8));

        setActionBar("Add New Device");
        load_data_from_add();
        getDoorCode();
        addBtn_Click();


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
            String code =(String) door_code.get("NewDoorCode");
            doorLock_Code.setText(code);
        }
    }


    private void firebaseDatabaseRecord(String device_name, String door_id, String user_name, String email_address, String password_field, Boolean lock_status) {
        UserDetails newUser = new UserDetails(users.getUid(), device_name, door_id, user_name, email_address, password_field,lock_status);

        mDataRef = database.getReference("/Users Details/"+ users.getUid());
        mDataRef.child(door_id).setValue(newUser);
    }


    private void addBtn_Click() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!users.equals(null)){
                    if(!deviceNameField.getText().equals(null)) {
                        firebaseDatabaseRecord(deviceNameField.getText().toString(), doorLock_Code.getText().toString(), username_shared, email_shared, password_shared, lock_Status);
                        notification("Successful add new door lock device!");
                        Intent intent = new Intent(AddNewDeviceActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        notification("Please fill the device name!");
                    }
                }
            }
        });

    }

    private void load_data_from_add() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        device_name_shared = sharedPref.getString("device_name", "");
        username_shared = sharedPref.getString("username", "");
        email_shared = sharedPref.getString("email", "");
        password_shared = sharedPref.getString("password", "");
        lock_status_shared = sharedPref.getBoolean("lock_status", false);
        door_lock_shared = sharedPref.getString("door_code", "");
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