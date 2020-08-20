package com.example.qrfacelocksystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.activity_splash_progressBar) ;
        progressBar.setVisibility(View.INVISIBLE);

        if(!isTaskRoot()){
            finish();
            return;
        }

        Thread background = new Thread() {
            public void run() {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    // Thread will sleep for 3 seconds
                    sleep(2000);

                    // After 3 seconds redirect to another intent
                    Intent intent = new Intent(SplashActivity.this,SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    progressBar.setVisibility(View.GONE);

                    //Remove activity
                    finish();
                } catch (Exception e) {
                }
            }
        };
        // start thread
        background.start();
    }
}