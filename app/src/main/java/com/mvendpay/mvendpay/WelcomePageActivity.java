package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import appcontroller.Config;

public class WelcomePageActivity extends AppCompatActivity {
    String url = AppController.url_base+"register";
    EditText etPhoneNumber;
    EditText etRegPassword;
    EditText etRegConfirmPassword;
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        Button btnRegister = (Button)findViewById(R.id.btnRegister);
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        etPhoneNumber  = (EditText)findViewById(R.id.etphoneNumber);
        etRegPassword  = (EditText)findViewById(R.id.etRegPassword);
        etRegConfirmPassword = (EditText)findViewById(R.id.etRegConfirmPassword);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber =  etPhoneNumber.getText().toString().trim();
                String password    =  etRegPassword.getText().toString().trim();
                String cPassword   = etRegConfirmPassword.getText().toString().trim();

                if(!validate( phoneNumber, password, cPassword)){
                    Toast.makeText(getBaseContext(), "Sign Up failed", Toast.LENGTH_LONG).show();
                    return;
                }

                progressDialog = new ProgressDialog(WelcomePageActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Creating Account...");
                progressDialog.show();
                register(phoneNumber, password,cPassword);

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent backIntent = new Intent(getBaseContext(),ChangeLoginAccountActivity.class);
                startActivity(backIntent);
                finish();

            }
        });
    }
    protected void register(final String phoneNumber, String password, String cPassword){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("phoneNumber", phoneNumber);
        map.put("password", password);
        map.put("confirmPassword", cPassword);

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
                                String merchant_id =  response.get("id").toString().trim();
                                progressDialog.dismiss();

                                editor.putBoolean("hasFilledDetails",false);
                                editor.putString("merchant_id",merchant_id);
                                editor.putString("phoneNumber",phoneNumber);
                                editor.commit();

                                Toast.makeText(getApplicationContext(),"Sign Up Successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getBaseContext(),RegisterVerificationCodeActivity.class);
                                startActivity(intent);
                                finish();
                            }else{

                                progressDialog.dismiss();
                                Toast.makeText(WelcomePageActivity.this,message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(WelcomePageActivity.this, "Error: " + error.getCause(), Toast.LENGTH_LONG).show();

            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }
    public boolean validate(String phoneNumber,String password,String cpassword) {

        boolean valid = true;

        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
          etPhoneNumber.setError("PhoneNumber must be 10 digits");
            valid = false;
        }else {
            etPhoneNumber.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            etRegPassword.setError("Should be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            etRegPassword.setError(null);
        }

        if (cpassword.isEmpty() || cpassword.length() < 4 || cpassword.length() > 10 || !(cpassword.equals(password))) {
            etRegConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            etRegConfirmPassword.setError(null);
        }

        return valid;
    }
}
