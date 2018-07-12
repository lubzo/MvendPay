package com.mvendpay.mvendpay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class SplashScreenActivity extends AppCompatActivity {
    private ProgressBar mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mProgress = (ProgressBar) findViewById(R.id.splash_screen_progress_bar);
        // Start lengthy operation in a background thread

        new Thread(new Runnable() {
            public void run() {
                doWork();

                SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
                Boolean hasFilledDetails = prefs.getBoolean("hasFilledDetails",false);

                if(hasFilledDetails){

                    Intent intentLogin = new Intent(getBaseContext(),ReturnWelcomePageActivity.class);
                    startActivity(intentLogin);
                }else{
                    Intent intentFillDetails =  new Intent(getBaseContext(),WelcomePageActivity.class);
                    startActivity(intentFillDetails);
                }
                finish();
            }
        }).start();
    }
    private void doWork() {
        for (int progress=0; progress<100; progress+=20) {
            try {
                Thread.sleep(500);
                mProgress.setProgress(progress);
            } catch (Exception e) {

            }
        }


    }


}
