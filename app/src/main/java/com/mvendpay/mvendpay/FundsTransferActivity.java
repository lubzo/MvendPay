package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import appcontroller.AppController;
import model.Wallet;

public class FundsTransferActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private String phoneNumber;
    private String transferAmount;
    EditText etTransferAmount;
    Spinner walletListSpinner;
    String transferUrl = AppController.url_base + "transfer";
    String loadWalletNumbers = AppController.url_base + "collectionWallets/";
    String merchant_id;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ArrayList<String> choiceList = new ArrayList<>();
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funds_transfer);
        mContext = this;

        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();

        etTransferAmount = (EditText) findViewById(R.id.etTransferAmount);

        Button btnFundsTransfersSubmit = (Button) findViewById(R.id.btnFundsTransfersSubmit);
        btnFundsTransfersSubmit.setOnClickListener(this);
        Button btnFundsTransferCancel = (Button) findViewById(R.id.btnFundsTransferCancel);
        btnFundsTransferCancel.setOnClickListener(this);

        walletListSpinner = (Spinner) findViewById(R.id.spn_linkedWallets);
        walletListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                phoneNumber = walletListSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        progressDialog = new ProgressDialog(FundsTransferActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("loading...");
        progressDialog.getWindow().setLayout(400, 50);
        progressDialog.show();


        loadWallets();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFundsTransfersSubmit:
                transferAmount = etTransferAmount.getText().toString().trim();

                if (!validate(phoneNumber, transferAmount)) {
                    Toast.makeText(mContext, "Error with input", Toast.LENGTH_SHORT).show();
                    return;
                }
                enterPin();
                break;
            case R.id.btnFundsTransferCancel:
                Intent intent = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private boolean validate(String phoneNumber, String transferAmount) {
        boolean valid = true;

        if (phoneNumber.isEmpty() || phoneNumber.length() == 0) {
            Toast.makeText(mContext, "Please select a phone number ", Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (transferAmount.isEmpty() || transferAmount.length() == 0) {
            etTransferAmount.setError("Please provide a valid amount");
            valid = false;
        } else {
            etTransferAmount.setError(null);
        }
        return valid;
    }

    private void enterPin() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.enter_pin, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AppTheme_White_Dialog);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText etPin = (EditText) promptsView.findViewById(R.id.etPin);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        String pin = etPin.getText().toString().trim();
                        if (pin.isEmpty() || pin.length() == 0) {
                            etPin.setError("Pin cannot be empty");
                            return;
                        } else {
                            etPin.setError(null);
                        }

                        progressDialog = new ProgressDialog(FundsTransferActivity.this,
                                R.style.AppTheme_White_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("loading...");
                        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progressDialog.show();
                        transfer(pin, phoneNumber, transferAmount);
                        alertDialog.dismiss();


                    }
                });
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        alertDialog.dismiss();
                    }
                });
            }
        });

        // show it
        TextView msg = new TextView(this);
        msg.setText("Confirm Transfer of " + transferAmount + " to " + phoneNumber);
        msg.setTextSize(14);
        msg.setPadding(5, 10, 5, 5);
        //msg.setTextColor();
        msg.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(msg);
        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void transfer(String pin, final String phoneNumber, final String transferAmount) {

        Date now = new Date();
        String format = new SimpleDateFormat("yyyyHHmmss", Locale.ENGLISH).format(now);
        String transactionID = format + merchant_id;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("pin", pin);
        map.put("phoneNumber", phoneNumber);
        map.put("transferAmount", transferAmount);
        map.put("transactionID", transactionID);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, transferUrl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                String acctBalance = response.get("account_balance").toString().trim();
                                int accountBalance = Integer.parseInt(acctBalance);
                                editor.putInt("accountBalance", accountBalance);
                                editor.commit();
                                Toast.makeText(FundsTransferActivity.this, "Transfer of " + transferAmount + " to " + phoneNumber + " Complete ", Toast.LENGTH_LONG).show();
                                etTransferAmount.setText("");
                                progressDialog.dismiss();
                                Intent intentMainMenu = new Intent(mContext, MainMenuActivity.class);
                                startActivity(intentMainMenu);

                            } else {
                                Toast.makeText(FundsTransferActivity.this, "Error in Transfer: " + message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FundsTransferActivity.this, "Server Error ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();

            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);

    }

    private void loadWallets() {
        JsonArrayRequest jsonArrayReq = new JsonArrayRequest(loadWalletNumbers + merchant_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {

                            try {
                                JSONObject obj = response.getJSONObject(i);

                                choiceList.add(obj.getString("msisdn").trim());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        if (response.length() == 0) {
                            progressDialog.dismiss();
                            android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(mContext);
                            builder1.setMessage("You have not set any collection wallets, Would you like to add a wallet?");
                            builder1.setCancelable(false);

                            builder1.setPositiveButton(
                                    "YES",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent intentMainMenu = new Intent(getBaseContext(), SetWalletActivity.class);
                                            startActivity(intentMainMenu);
                                        }
                                    });

                            builder1.setNegativeButton(
                                    "NO",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            finish();
                                        }
                                    });

                            android.app.AlertDialog alert11 = builder1.create();
                            alert11.show();
                            Toast.makeText(FundsTransferActivity.this, "No Available Wallets ", Toast.LENGTH_SHORT).show();

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                                android.R.layout.simple_spinner_dropdown_item, choiceList);

                        walletListSpinner.setAdapter(adapter);
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(FundsTransferActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayReq);

    }
}
