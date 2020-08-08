package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import java.util.regex.Pattern;


public class NewSetupActivity extends AppCompatActivity {

    private static final String TAG = "NewSetup";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private TextView doorLock_Code;
    private EditText usernameField, emailField, passwordField;
    private Button setupButton;
    private CheckBox showPasswordCheckbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsetup);

        doorLock_Code = (TextView) findViewById(R.id.door_code);

        usernameField = (EditText) findViewById(R.id.nameTextField);
        emailField = (EditText) findViewById(R.id.emailTextField);
        passwordField = (EditText) findViewById(R.id.passwordTextField);

        setupButton = (Button) findViewById(R.id.setupButton);

        showPasswordCheckbox = (CheckBox)findViewById(R.id.showPassCheckBox);

        doorLock_Code.setText(GenerateRandomString.randomString(8));


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


    private void firebaseAuthSignUp(final String email_address, final String password_field) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if (validateEmail() | validatePassword()) {
            mAuth.fetchSignInMethodsForEmail(emailField.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                            boolean check = !task.getResult().getSignInMethods().isEmpty();

                            if (!check) {
                                firebaseSignUp(email_address,password_field);
                            } else {
                                emailField.setError("Email address already been used!");
                            }
                        }
                    });
        }
    }



    private void firebaseSignUp(String email_address, String password_field) {

        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email_address,password_field)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            notification("Setup Successful!");
                            firebaseDatabaseRecord(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString());
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            AlertDialog.Builder alert1 = new AlertDialog.Builder(NewSetupActivity.this);
                            alert1.setTitle("Setup Failed");
                            alert1.setMessage("Setup failed, Please try again!");
                            alert1.setPositiveButton("Try Again",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            emailField.setText("");
                                            passwordField.setFocusable(true);
                                        }
                                    });
                            alert1.show();
//                            updateUI(null);
                        }

                    }
                });
    }

    private void firebaseDatabaseRecord(String user_name, String email_address, String password_field) {


        UserDetails newUser = new UserDetails(doorLock_Code.getText().toString(),user_name,email_address,password_field);

        database = FirebaseDatabase.getInstance();
        mDataRef = database.getReference("/Users Details");
        mDataRef.child(doorLock_Code.getText().toString()).setValue(newUser, new completionListener());
    }


    private void setupBtn_Click() {
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuthSignUp(emailField.getText().toString(),passwordField.getText().toString());

            }
        });

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



    private class completionListener implements DatabaseReference.CompletionListener{

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

    private boolean validateEmail(){

        String emailInput = emailField.getText().toString().trim();

        if(emailInput.isEmpty()){
            emailField.setError("Field can't be empty!");
            return false;
        }else if(!Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(emailInput).matches()){
            emailField.setError("Please enter a valid email address");
            return false;
        }else{
            emailField.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String passwordInput = passwordField.getText().toString().trim();

        if(passwordInput.isEmpty()){
            passwordField.setError("Field can't be empty!");
            return false;
        }else if(!Pattern.compile("^.{8,16}$").matcher(passwordInput).matches()){
            passwordField.setError("Password must between 8 to 16 character");
            return false;
        }else{
            passwordField.setError(null);
            return true;
        }
    }

        public class UserDetails {

            public String username;
            public String email;
            public String password;
            public String doorId;

            public UserDetails() {
                // Default constructor required for calls to DataSnapshot.getValue(User.class)
            }

            public UserDetails(String door_id, String username, String email, String password) {
                this.doorId = door_id;
                this.username = username;
                this.email = email;
                this.password = password;
            }
        }

        public static class GenerateRandomString {

            public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            public static Random RANDOM = new Random();

            public static String randomString(int len) {
                StringBuilder sb = new StringBuilder(len);

                for (int i = 0; i < len; i++) {
                    sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
                }

                return sb.toString();
            }

        }

//    database = FirebaseDatabase.getInstance();
//    mDataRef = database.getReference("/Users Details/Door Lock ID/");

}