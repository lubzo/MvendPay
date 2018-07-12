package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class RegisterUserDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEnterpriseName;
    EditText etFullName;
    EditText etNationalId;
    EditText etTinNumber;
    Spinner businessTypeSpinner;
    String businessType = "";
    String merchant_id;
    String phoneNumber;
    String isMerchant;
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    String url = AppController.url_base+"registerUserDetails";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_details);
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);

        merchant_id = prefs.getString("merchant_id", null);
        phoneNumber = prefs.getString("phoneNumber", null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button btnRegisterUserCancel = (Button) findViewById(R.id.btnRegisterUserCancel);
        Button btnSubmitUserDetails  = (Button) findViewById(R.id.btnSubmitUserDetails);

        btnSubmitUserDetails.setOnClickListener(this);
        btnRegisterUserCancel.setOnClickListener(this);


        etFullName = (EditText)findViewById(R.id.etFullName);
        etNationalId = (EditText)findViewById(R.id.etNationalId);
        etTinNumber = (EditText)findViewById(R.id.etTinNumber);
        etEnterpriseName = (EditText)findViewById(R.id.etEnterpriseName);
        businessTypeSpinner = (Spinner) findViewById(R.id.spn_Business_type);

        final String[] items = new String[] { "Select business type","Individual", "Enterprise" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);

        businessTypeSpinner.setAdapter(adapter);
        businessTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if(items[position].trim().equals("Enterprise")) {
                    businessType = "Enterprise";
                   // isMerchant = "1";
                    etEnterpriseName.setVisibility(View.VISIBLE);
                }else if(items[position].trim().equals("Individual")){
                    businessType = "Individual";
                    //isMerchant = "0";
                    etEnterpriseName.setVisibility(View.GONE);
                }else if(items[position].trim().equals("Select business type")){
                    etEnterpriseName.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btnSubmitUserDetails:

                String  fullName = etFullName.getText().toString().trim();
                String  nationalID = etNationalId.getText().toString().trim();
                String tinNumber = etTinNumber.getText().toString().trim();
                String enterpriseName = etEnterpriseName.getText().toString().trim();

                if(!validate(fullName,nationalID,tinNumber,enterpriseName,businessType)){
                    Toast.makeText(getBaseContext(), "User detail entry failed", Toast.LENGTH_LONG).show();
                    return;
                }

                progressDialog = new ProgressDialog(RegisterUserDetailsActivity.this,
                        R.style.AppTheme_White_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Adding User Details ...");
                progressDialog.show();

                submitUserDetails(merchant_id,phoneNumber,
                        fullName,nationalID,tinNumber,
                        enterpriseName,businessType);

                break;
            case R.id.btnRegisterUserCancel:

                Intent intent1 = new Intent(this,WelcomePageActivity.class);
                startActivity(intent1);
                finish();

                break;

        }
    }

    private boolean validate(String fullName, String nationalID, String tinNumber, String enterpriseName, String businessType) {

        boolean valid = true;

        if (fullName.isEmpty() || fullName.length() == 0) {
            etFullName.setError("Full Name code cannot be empty");
            valid = false;
        }
        if (nationalID.isEmpty() || nationalID.length() == 0) {
            etNationalId.setError("National ID cannot be empty");
            valid = false;
        }
        if (tinNumber.isEmpty() || tinNumber.length() == 0) {
            etTinNumber.setError("Tin Number cannot be empty");
            valid = false;
        }
        if( businessType.isEmpty()){
            Toast.makeText(getBaseContext(),"Business Type cannot be empty",Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (businessType.equals("Enterprise") && (enterpriseName.isEmpty() || enterpriseName.length() == 0)) {
            etEnterpriseName.setError("Enterprise Name cannot be empty");
            valid = false;
        }

        return valid;
    }

    private void submitUserDetails(String merchant_id, String phoneNumber,
                                   final String fullName, String nationalID, String tinNumber,
                                   final String enterpriseName, String businessType) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("phoneNumber", phoneNumber);
        map.put("fullName", fullName);
        map.put("nationalID", nationalID);
        map.put("tinNumber", tinNumber);
        map.put("enterpriseName", enterpriseName);
        map.put("businessType", businessType);
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
                                editor.putBoolean("hasFilledDetails",true);
                                editor.putString("fullName",fullName);
                                editor.putString("enterprise_name",enterpriseName);
                                editor.putInt("accountBalance",0);
                                editor.putBoolean("hasSetPin",false);
                                editor.commit();

                                Intent intent = new Intent(getBaseContext(),MainMenuActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(RegisterUserDetailsActivity.this, message , Toast.LENGTH_LONG).show();
                            }else{
                                progressDialog.dismiss();

                                Toast.makeText(RegisterUserDetailsActivity.this, "Error, Please Try again " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserDetailsActivity.this, " Error, Please Try again "+e.getMessage(), Toast.LENGTH_LONG).show();
                             e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(RegisterUserDetailsActivity.this, "error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }


}
