package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ChangeLoginAccountActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etLoginChangePhoneNumber;
    EditText etLoginChangePassword;
    String url = AppController.url_base+"login";
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_login_account);
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        etLoginChangePassword = (EditText)findViewById(R.id.etLoginChangePassword);
        etLoginChangePhoneNumber = (EditText)findViewById(R.id.etLoginChangePhoneNumber);

        Button btnLoginChangeRegister = (Button)findViewById(R.id.btnLoginChangeRegister);
        Button btnLoginChangeSubmit  =(Button)findViewById(R.id.btnLoginChangeSubmit);
        btnLoginChangeRegister.setOnClickListener(this);
        btnLoginChangeSubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnLoginChangeRegister:

                Intent intentRegister = new Intent(getBaseContext(),WelcomePageActivity.class);
                startActivity(intentRegister);
                finish();
                break;
            case R.id.btnLoginChangeSubmit:

                String password = etLoginChangePassword.getText().toString().trim();
                String phoneNumber = etLoginChangePhoneNumber.getText().toString().trim();
                if(!validate(phoneNumber,password)){
                    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog = new ProgressDialog(ChangeLoginAccountActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Logging in...");
                progressDialog.show();
                Login(phoneNumber,password);

                break;


        }
    }

    private boolean validate(String phoneNumber, String password) {
        boolean valid = true;
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {

            etLoginChangePassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;

        } else {
            etLoginChangePassword.setError(null);
        }

        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            etLoginChangePhoneNumber.setError("PhoneNumber must be 10 digits");
            valid = false;
        }else{
            etLoginChangePhoneNumber.setError(null);
        }
        return  valid;
    }
    private void Login(final String phoneNumber, String password) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("phoneNumber", phoneNumber);
        map.put("password", password);

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
                                progressDialog.dismiss();
                                int accountBalance = Integer.parseInt(response.get("accountBalance").toString().trim());
                                String merchant_id= response.get("merchant_id").toString().trim();
                                String fullName = response.get("fullName").toString().trim();
                                String enterpriseName = response.get("enterprise_name").toString().trim();
                                boolean hasSetPin    =   Boolean.valueOf(response.get("hasSetPin").toString().trim());
                                editor.putBoolean("hasFilledDetails",true);
                                editor.putString("enterprise_name",enterpriseName);
                                editor.putInt("accountBalance",accountBalance);
                                editor.putString("merchant_id",merchant_id);
                                editor.putString("phoneNumber",phoneNumber);
                                editor.putString("fullName",fullName);
                                editor.putBoolean("hasSetPin",hasSetPin);
                                editor.commit();

                                Toast.makeText(getApplicationContext(),"Login Successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getBaseContext(),MainMenuActivity.class);
                                startActivity(intent);
                                finish();
                            }else{

                                progressDialog.dismiss();
                                Toast.makeText(ChangeLoginAccountActivity.this,message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(ChangeLoginAccountActivity.this, "error" + error.getCause(), Toast.LENGTH_LONG).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }


}
