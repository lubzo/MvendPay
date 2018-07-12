package com.mvendpay.mvendpay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import appcontroller.Config;

public class TouchPayActivity extends AppCompatActivity implements View.OnClickListener {

    String merchant_id;
    EditText etTouchPayAmount;
    EditText etTouchPayNarrative;
    String transactionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_pay);
        Button btnTouchPaySubmitRequest = (Button) findViewById(R.id.btnTouchPaySubmitRequest);
        btnTouchPaySubmitRequest.setOnClickListener(this);

        Button btnTouchPayCancel = (Button) findViewById(R.id.btnTouchPayCancel);
        btnTouchPayCancel.setOnClickListener(this);

        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);


        Date now = new Date();
        String format = new SimpleDateFormat("yyyyHHmmss", Locale.ENGLISH).format(now);

        TextView tvTransactionID = (TextView) findViewById(R.id.etTouchPayTransactionID);
        transactionID =  format+merchant_id;
        tvTransactionID.setText(transactionID);

        etTouchPayAmount = (EditText) findViewById(R.id.etTouchPayAmount);
        etTouchPayNarrative = (EditText) findViewById(R.id.etTouchPayNarrative);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnTouchPayCancel:

                Intent intentMainMenu = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(intentMainMenu);
               // finish();
                break;

            case R.id.btnTouchPaySubmitRequest:

                String touchPayAmount = etTouchPayAmount.getText().toString().trim();

                if (!validate(touchPayAmount)) {
                    return;
                }

                Intent intentTransactionSummary = new Intent(getBaseContext(), TransactionSummaryActivity.class);
                intentTransactionSummary.putExtra("transactionID", transactionID);
                intentTransactionSummary.putExtra("TouchPayAmount", etTouchPayAmount.getText().toString().trim());
                intentTransactionSummary.putExtra("TouchPayNarrative", etTouchPayNarrative.getText().toString().trim());
                intentTransactionSummary.putExtra("merchant_id", merchant_id);
                startActivity(intentTransactionSummary);

                break;

        }


    }

    private boolean validate(String touchPayAmount) {
        boolean valid = true;
        if (touchPayAmount.isEmpty() || touchPayAmount.equals("")) {

            etTouchPayAmount.setError("Amount Cannot be Empty");
            valid = false;
        } else {
            etTouchPayAmount.setError(null);
        }

        return valid;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
