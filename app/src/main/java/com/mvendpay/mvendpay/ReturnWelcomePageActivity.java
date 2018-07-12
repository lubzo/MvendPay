package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import appcontroller.AppController;
import appcontroller.Config;

public class ReturnWelcomePageActivity extends AppCompatActivity implements View.OnClickListener {
    String phoneNumber;
    EditText etLoginPassword;
    String url = AppController.url_base+"login";
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_welcome_page);
        editor                  = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        String fullName         = prefs.getString("fullName",null);
        phoneNumber             = prefs.getString("phoneNumber",null);

        TextView tvCurrentPhoneNumber = (TextView) findViewById(R.id.tvCurrentPhoneNumber);
        tvCurrentPhoneNumber.setText(" Access account for " + phoneNumber);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Hi " + fullName);

        Button btnLoginCancel = (Button) findViewById(R.id.btnLoginCancel);
        btnLoginCancel.setOnClickListener(this);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);


        etLoginPassword = (EditText) findViewById(R.id.etLoginPassword);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.tvChange:
                Intent intent = new Intent(getBaseContext(),ChangeLoginAccountActivity.class);
                startActivity(intent);
               // finish();
                break;

            case R.id.btnLogin:

                String password = etLoginPassword.getText().toString().trim();
                if(!validate(password)){
                    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog = new ProgressDialog(ReturnWelcomePageActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Logging in...");
                progressDialog.show();
                Login(phoneNumber,password);

                break;

            case R.id.btnLoginCancel:
                finish();
                break;

        }

    }

    private boolean validate(String password) {

        boolean valid =true;

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {

            etLoginPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;

        } else {
            etLoginPassword.setError(null);
        }

        return valid;
    }


    private void Login(String phoneNumber, String password) {

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
                                editor.putInt("accountBalance",accountBalance);
                                boolean hasSetPin    =   Boolean.valueOf(response.get("hasSetPin").toString().trim());
                                editor.putBoolean("hasSetPin",hasSetPin);
                                editor.commit();

                                Toast.makeText(getApplicationContext(),"Login Successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getBaseContext(),MainMenuActivity.class);
                                startActivity(intent);
                                finish();
                            }else{

                                progressDialog.dismiss();
                                Toast.makeText(ReturnWelcomePageActivity.this,message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(ReturnWelcomePageActivity.this, "Error: " + error.getCause(), Toast.LENGTH_LONG).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);

    }
}
