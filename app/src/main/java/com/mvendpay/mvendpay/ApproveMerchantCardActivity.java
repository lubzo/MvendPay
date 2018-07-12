package com.mvendpay.mvendpay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import appcontroller.AppController;

public class ApproveMerchantCardActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtMessage;
    private Context mContext;
    String amount;
    String merchant_buyer;
    String merchant_id;
    String transactionID;
    String narrative;
    EditText etPin;
    String pin;
    SharedPreferences.Editor editor;
    private static final String TAG = ApproveMerchantCardActivity.class.getSimpleName();
    String url = AppController.url_base + "approvePayment";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_approve_merchant_card);
        RelativeLayout mainLayout=(RelativeLayout)this.findViewById(R.id.approveLayout);
        this.setFinishOnTouchOutside(false);
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        etPin      = (EditText) findViewById(R.id.etApprovePin);
        txtMessage = (TextView) findViewById(R.id.txt_push_message);

        mContext = this;

        if (getIntent().getExtras() != null) {
            final String data =  getIntent().getStringExtra("message");

            if(data.contains("ReceiptNo")){

                mainLayout.setVisibility(RelativeLayout.GONE);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("Payment Received and Confirmed");
                builder1.setMessage(data);
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);

                                int accountBalance =  prefs.getInt("accountBalance",0);
                                int pos            =  data.indexOf("Amount:");
                                int pos1           =  data.indexOf("PaymentRef");

                                int amt = Integer.parseInt(data.substring((pos +7),pos1).trim());
                                accountBalance = accountBalance + amt;

                                editor.putInt("accountBalance", accountBalance);
                                editor.commit();


                                finish();
                            }
                        });


                AlertDialog alert11 = builder1.create();
                alert11.show();

            }else {

                amount = getIntent().getStringExtra("amount");
                merchant_buyer = getIntent().getStringExtra("merchant_buyer");
                merchant_id = getIntent().getStringExtra("merchant_id");
                narrative = getIntent().getStringExtra("narrative");
                transactionID = getIntent().getStringExtra("transactionID");
                txtMessage.setText(data);
            }
        }

        Button btnApproveCancel = (Button)findViewById(R.id.btnApproveCancel);
        Button btnApprove = (Button)findViewById(R.id.btnApprove);
        btnApprove.setOnClickListener(this);
        btnApproveCancel.setOnClickListener(this);

    }



    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
    @Override
    public void onBackPressed() {

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnApprove:

                pin = etPin.getText().toString().trim();

                approve(merchant_buyer,merchant_id,pin);
                break;
            case R.id.btnApproveCancel:
                finish();
                break;
        }
    }

    public void approve(String merchant_buyer,String merchant_id, String pin){

        if (!validate(pin)) {
            Toast.makeText(getApplicationContext(), "Error with input", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = new ProgressDialog(ApproveMerchantCardActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("loading...");
        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_buyer", merchant_buyer);
        map.put("merchant_id", merchant_id);
        map.put("pin", pin.trim());
        map.put("amount", amount);
        map.put("narrative",narrative );
        map.put("transactionID", transactionID);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                Toast.makeText(getApplicationContext(), "Payment Successful for transactionID : "+ transactionID, Toast.LENGTH_LONG).show();

                                SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);

                                int accountBalance = prefs.getInt("accountBalance",0);
                                int amt = Integer.parseInt(amount);
                                accountBalance = accountBalance - amt;

                                editor.putInt("accountBalance", accountBalance);
                                editor.commit();



                                progressDialog.dismiss();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error,Please try again: " + message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Server Error ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }
    private boolean validate(String Pin) {
        boolean valid = true;
        if (Pin.isEmpty() || Pin.trim().length() == 0) {
            etPin.setError("Pin cannot be empty");
            valid = false;
        } else {
            etPin.setError(null);
        }

        return valid;
    }

}

