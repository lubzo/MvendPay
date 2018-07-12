package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
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

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    String updatePasswordUrl = AppController.url_base + "updatePassword";
    String merchant_id;
    ProgressDialog progressDialog;
    EditText etChangePasswordOld;
    EditText etChangePasswordNew;
    EditText etChangePasswordNewConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);

        etChangePasswordOld = (EditText) findViewById(R.id.etChangePasswordOld);
        etChangePasswordNew = (EditText) findViewById(R.id.etChangePasswordNew);
        etChangePasswordNewConfirm = (EditText) findViewById(R.id.etChangePasswordNewConfirm);

        Button btnChangePasswordCancel = (Button)findViewById(R.id.btnChangePasswordCancel);
        Button btnChangePasswordSubmit = (Button) findViewById(R.id.btnChangePasswordSubmit);
        btnChangePasswordSubmit.setOnClickListener(this);
        btnChangePasswordCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnChangePasswordCancel:
                Intent intentMainMenu = new Intent(getBaseContext(),MainMenuActivity.class);
                startActivity(intentMainMenu);
                finish();
                break;
            case R.id.btnChangePasswordSubmit:

                String oldPassword = etChangePasswordOld.getText().toString().trim();
                String confirmNewPassword = etChangePasswordNewConfirm.getText().toString().trim();
                String newPassword = etChangePasswordNew.getText().toString().trim();

                if (!validate(oldPassword, confirmNewPassword, newPassword)) {
                    return;
                }

                progressDialog = new ProgressDialog(ChangePasswordActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("loading...");
                progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressDialog.show();

                updatePassword(oldPassword,newPassword);
                break;

        }
    }

    private boolean validate(String oldPassword, String confirmNewPassword, String newPassword) {
        boolean valid = true;

        if (oldPassword.isEmpty() || oldPassword.length() < 4 || oldPassword.length() > 10) {
            etChangePasswordOld.setError("Should be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            etChangePasswordOld.setError(null);
        }

        if (newPassword.isEmpty() || newPassword.length() < 4 || newPassword.length() > 10) {
            etChangePasswordNew.setError("Should be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            etChangePasswordNew.setError(null);
        }

        if (confirmNewPassword.isEmpty() || confirmNewPassword.length() < 4 || confirmNewPassword.length() > 10) {
            etChangePasswordNewConfirm.setError("Should be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            etChangePasswordNewConfirm.setError(null);
        }

        if (!confirmNewPassword.equals(newPassword)) {
            etChangePasswordNewConfirm.setError("Passwords must match");
            valid = false;
        }else{
            etChangePasswordNewConfirm.setError(null);
        }

        return valid;
    }

    private void updatePassword(String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("merchant_id", merchant_id);
        map.put("pwd", newPassword);
        map.put("oldPwd", oldPassword);


        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, updatePasswordUrl, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                Toast.makeText(ChangePasswordActivity.this, "Password Updated Successfully", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                Intent intentMainMenu = new Intent(getBaseContext(),MainMenuActivity.class);
                                startActivity(intentMainMenu);
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChangePasswordActivity.this, "error" + error.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

}
