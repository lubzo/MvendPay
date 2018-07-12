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
import android.widget.Button;
import android.widget.EditText;
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

public class MyAccountActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressDialog progressDialog;
    private Context mContext;
    String merchant_id;
    String enableCardUrl = AppController.url_base + "authorizeCard";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        mContext = this;
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);
        Button btnChangePin      = (Button)findViewById(R.id.btnChangePin);
        Button btnChangePassword = (Button)findViewById(R.id.btnChangePassword);
        Button btnChangeEnableCard = (Button)findViewById(R.id.btnChangeEnableCard);

        btnChangePassword.setOnClickListener(this);
        btnChangePin.setOnClickListener(this);
        btnChangeEnableCard.setOnClickListener(this);
}

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnChangePin:
                Intent changePin = new Intent(getBaseContext(),ChangePinActivity.class);
                startActivity(changePin);
                break;
            case R.id.btnChangePassword:
                Intent changePassword = new Intent(getBaseContext(),ChangePasswordActivity.class);
                startActivity(changePassword);
                break;
            case R.id.btnChangeEnableCard:
                enterPin();

                break;
        }
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

                        progressDialog = new ProgressDialog(MyAccountActivity.this,
                                R.style.AppTheme_White_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("loading...");
                        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progressDialog.show();

                        enableCard(pin,merchant_id);

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
        msg.setText("Confirm Pin");
        msg.setTextSize(14);
        msg.setPadding(5, 10, 5, 5);
        msg.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(msg);
        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void enableCard(String pin, String merchant_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("pin", pin);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, enableCardUrl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                Toast myToast = Toast.makeText(getApplicationContext(), "Card Enabled Successfully",Toast.LENGTH_LONG);
                                myToast.setGravity(Gravity.TOP,0,300);
                                myToast.show();
                                progressDialog.dismiss();
                                Intent intentMainMenu = new Intent(mContext, MainMenuActivity.class);
                                startActivity(intentMainMenu);

                            } else {
                                Toast myToast = Toast.makeText(getApplicationContext(), "Error : " + message,Toast.LENGTH_LONG);
                                myToast.setGravity(Gravity.TOP,0,300);
                                myToast.show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast myToast = Toast.makeText(getApplicationContext(), "Server Error",Toast.LENGTH_LONG);
                myToast.setGravity(Gravity.TOP,0,300);
                myToast.show();
                progressDialog.dismiss();

            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
