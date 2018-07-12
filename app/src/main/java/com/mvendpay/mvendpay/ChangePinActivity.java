package com.mvendpay.mvendpay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangePinActivity extends AppCompatActivity implements View.OnClickListener {

    String updatePinUrl = AppController.url_base + "updatePin";
    String merchant_id;
    ProgressDialog progressDialog;
    boolean hasSetPin = false;
    EditText etChangePinOld;
    EditText etChangePinNew;
    EditText etChangePinNewConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);
        hasSetPin = prefs.getBoolean("hasSetPin", false);


        etChangePinOld = (EditText) findViewById(R.id.etChangePinOld);
        etChangePinNew = (EditText) findViewById(R.id.etChangePinNew);
        etChangePinNewConfirm = (EditText) findViewById(R.id.etChangePinNewConfirm);

        Button btnChangePinCancel = (Button) findViewById(R.id.btnChangePinCancel);
        Button btnChangePinSubmit = (Button) findViewById(R.id.btnChangePinSubmit);
        btnChangePinSubmit.setOnClickListener(this);
        btnChangePinCancel.setOnClickListener(this);

        if(!hasSetPin){

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("You do not have a set Pin, Would You like to set your pin?");
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            Intent intentMainMenu = new Intent(getBaseContext(),SetWalletActivity.class);
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

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChangePinCancel:
                Intent intentMainMenu = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(intentMainMenu);
                finish();
                break;
            case R.id.btnChangePinSubmit:

                String oldPin = etChangePinOld.getText().toString().trim();
                String confirmNewPin = etChangePinNewConfirm.getText().toString().trim();
                String newPin = etChangePinNew.getText().toString().trim();

                if (!validate(oldPin, confirmNewPin, newPin)) {
                    return;
                }

                progressDialog = new ProgressDialog(ChangePinActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("loading...");
                progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressDialog.show();

                updatePin(oldPin,newPin);

                break;

        }
    }

    private boolean validate(String oldPin, String confirmNewPin, String newPin) {
        boolean valid = true;

        if (oldPin.isEmpty() || oldPin.length() < 4) {
            etChangePinOld.setError("Pin should have more than 4 digits");
            valid = false;
        } else {
            etChangePinOld.setError(null);
        }

        if (newPin.isEmpty() || newPin.length() < 4) {
            etChangePinNew.setError("Pin should have more than 4 digits");
            valid = false;
        } else {
            etChangePinNew.setError(null);
        }

        if (confirmNewPin.isEmpty() || confirmNewPin.length() < 4) {
            etChangePinNewConfirm.setError("Pin should have more than 4 digits");
            valid = false;
        } else {
            etChangePinNewConfirm.setError(null);
        }

        if (!confirmNewPin.equalsIgnoreCase(newPin)) {
            etChangePinNewConfirm.setError("Pins must match");
            valid = false;
        }else{
            etChangePinNewConfirm.setError(null);
        }

        return valid;
    }

    private void updatePin(String oldPin, String newPin) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("merchant_id", merchant_id);
        map.put("pin", newPin);
        map.put("oldPin", oldPin);


        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, updatePinUrl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                Toast.makeText(ChangePinActivity.this, "Pin Updated Successfully", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                Intent intentMainMenu = new Intent(getBaseContext(),MainMenuActivity.class);
                                startActivity(intentMainMenu);
                                finish();
                            } else {
                                Toast.makeText(ChangePinActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChangePinActivity.this, "error" + error.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }
}
