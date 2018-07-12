package com.mvendpay.mvendpay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TransactionReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_receipt);
        String transactionID = getIntent().getStringExtra("transactionID");
        String transactionAmount = getIntent().getStringExtra("transactionAmount");
        String narrative = getIntent().getStringExtra("narrative");
        String paymentRef = getIntent().getStringExtra("paymentRef");
        String receiptNumber = getIntent().getStringExtra("receiptNumber");

        TextView tvTransactionCompleteID = (TextView) findViewById(R.id.tvTransactionCompleteID);
        tvTransactionCompleteID.setText(transactionID);

        TextView tvTransactionCompleteAmount = (TextView) findViewById(R.id.tvTransactionCompleteAmount);
        tvTransactionCompleteAmount.setText(transactionAmount);

        TextView tvTransactionCompleteNarrative = (TextView) findViewById(R.id.tvTransactionCompleteNarrative);
        tvTransactionCompleteNarrative.setText(narrative);

        TextView tvTransactionCompletePaymentRef = (TextView) findViewById(R.id.tvTransactionCompletePaymentRef);
        tvTransactionCompletePaymentRef.setText(paymentRef);

        TextView tvTransactionCompleteReceipt = (TextView) findViewById(R.id.tvTransactionCompleteReceipt);
        tvTransactionCompleteReceipt.setText(receiptNumber);


        Button btnTransactionReceipt = (Button) findViewById(R.id.btnTransactionCompleteReceipt);
        btnTransactionReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainMenu = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(intentMainMenu);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
