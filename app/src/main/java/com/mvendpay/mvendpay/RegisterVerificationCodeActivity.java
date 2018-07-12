package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
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

import java.util.HashMap;
import java.util.Map;

import appcontroller.AppController;

public class RegisterVerificationCodeActivity extends AppCompatActivity implements View.OnClickListener {
    String url = AppController.url_base+"verifyCode";
    String merchant_id;
    String phoneNumber;
    EditText etRegVerificationCode;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_verification_code);
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);
        phoneNumber = prefs.getString("phoneNumber", null);
        TextView tvPhoneNumber =  (TextView)findViewById(R.id.textView6) ;

        //hide phoneNumber
        String hiddenNumber = "XXXX XXX " + phoneNumber.substring(7,phoneNumber.length());

        tvPhoneNumber.setText(hiddenNumber);

        etRegVerificationCode = (EditText)findViewById(R.id.etRegVerificationCode) ;
        Button btnRegVerificationCancel = (Button) findViewById(R.id.btnRegVerificationCancel);
        Button btnSubmitCode = (Button) findViewById(R.id.btnSubmitCode);
        btnRegVerificationCancel.setOnClickListener(this);
        btnSubmitCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnSubmitCode:

                String code = etRegVerificationCode.getText().toString().trim();

                if(!validate(code)){
                    Toast.makeText(getBaseContext(), "Input Error", Toast.LENGTH_LONG).show();
                    return;
                }


                progressDialog = new ProgressDialog(RegisterVerificationCodeActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Verifying Code ...");
                progressDialog.show();
                verifyCode(code, merchant_id);

                break;
            case R.id.btnRegVerificationCancel:
                Intent intent1 = new Intent(this,WelcomePageActivity.class);
                startActivity(intent1);
                finish();
                break;
        }

    }

    private boolean validate(String code) {
        boolean valid = true;

        if (code.isEmpty() || code.length() == 0) {
            etRegVerificationCode.setError("Verification code cannot be empty");
            valid = false;
        }
        return valid;
    }

    private void verifyCode(String code, String merchant_id){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("code", code);
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
                                Intent intent = new Intent(getBaseContext(),RegisterUserDetailsActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(RegisterVerificationCodeActivity.this, message , Toast.LENGTH_LONG).show();

                            }else{
                                progressDialog.dismiss();
                                etRegVerificationCode.setError("Please enter code again");
                                Toast.makeText(RegisterVerificationCodeActivity.this, "Error, Please Try again", Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterVerificationCodeActivity.this, "Error, Please Try again", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(RegisterVerificationCodeActivity.this, "error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);


    }
}
