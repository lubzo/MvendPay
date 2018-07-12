package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.MainMenuAdapter;
import appcontroller.AppController;
import appcontroller.Config;
import model.MainMenuItem;

public class MainMenuActivity extends AppCompatActivity {

    private String merchant_id;
    private String token;
    private String updateFireBaseID = AppController.url_base + "updateFireBaseID";
    ProgressDialog progressDialog;
    TextView tvAccountBalance;
    private List<MainMenuItem> menuItemList = new ArrayList<MainMenuItem>();
    private ListView listView;
    private MainMenuAdapter adapter;
    TextView tvMerchantName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        tvAccountBalance = (TextView)findViewById(R.id.tvAccountBalance);
        tvMerchantName = (TextView)findViewById(R.id.tvMerchantName);
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);


        String enterprise_name = prefs.getString("enterprise_name",null);
        if(enterprise_name == null || enterprise_name.isEmpty() || enterprise_name.trim().equals("")){
            String fullName    = prefs.getString("fullName",null);
            tvMerchantName.setText(fullName.substring(0, 1).toUpperCase() + fullName.substring(1));
        }else{
            tvMerchantName.setText(enterprise_name.substring(0, 1).toUpperCase() + enterprise_name.substring(1));
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        token = pref.getString("regId", null);

        // send Fire base ID
        progressDialog = new ProgressDialog(MainMenuActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("loading...");
        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();

        sendRegistrationToServer(token,merchant_id);

        int accountBalance = prefs.getInt("accountBalance",0);
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedTotal = formatter.format(accountBalance);
        tvAccountBalance.setText(formattedTotal);


        MainMenuItem m1 = new MainMenuItem();
        m1.setTitle("Touch Pay");
        m1.setSubTitle("Make a Payment");
        m1.setDrawable(R.drawable.circle1);
        menuItemList.add(m1);

        MainMenuItem m2 = new MainMenuItem();
        m2.setTitle("Account");
        m2.setSubTitle("Manage Account");
        m2.setDrawable(R.drawable.circle2);
        menuItemList.add(m2);

        MainMenuItem m3 = new MainMenuItem();
        m3.setTitle("Set Wallet");
        m3.setSubTitle("Change/add wallet preferences");
        m3.setDrawable(R.drawable.circle3);
        menuItemList.add(m3);

        MainMenuItem m4 = new MainMenuItem();
        m4.setTitle("History");
        m4.setSubTitle("see account transactions");
        m4.setDrawable(R.drawable.circle4);
        menuItemList.add(m4);

        MainMenuItem m5 = new MainMenuItem();
        m5.setTitle("Money Transfer");
        m5.setSubTitle("Transfer money from Account");
        m5.setDrawable(R.drawable.circle5);
        menuItemList.add(m5);

        MainMenuItem m6 = new MainMenuItem();
        m6.setTitle("Log Out");
        m6.setSubTitle("Log out of App");
        m6.setDrawable(R.drawable.circle6);
        menuItemList.add(m6);

        listView = (ListView) findViewById(R.id.main_menu_list);
        adapter = new MainMenuAdapter(this, menuItemList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position){

                    case 0:
                        Intent intentTouchPay = new Intent(getBaseContext(),TouchPayActivity.class);
                        startActivity(intentTouchPay);
                        // finish();
                        break;

                    case 1:
                        Intent intentMyAccount = new Intent(getBaseContext(),MyAccountActivity.class);
                        startActivity(intentMyAccount);
                        // finish();
                        break;

                    case 2:

                        Intent intentWallet = new Intent(getBaseContext(),SetWalletActivity.class);
                        startActivity(intentWallet);
                        // finish();
                        break;

                    case 3:
                        Intent intentTransactionHistory =  new Intent(getBaseContext(),AccountHistoryActivity.class);
                        startActivity(intentTransactionHistory);
                        //finish();
                        break;

                    case 4:
                        Intent intentLiquidation = new Intent(getBaseContext(),FundsTransferActivity.class);
                        startActivity(intentLiquidation);
                        //finish();
                        break;

                    case 5:
                        Intent intentLogin = new Intent(getBaseContext(),ReturnWelcomePageActivity.class);
                        // Closing all the Activities
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //  intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        // Add new Flag to start new Activity
                        // intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity(intentLogin);
                        finish();
                        break;
                }
            }
        });



    }



    @Override
    protected void onPause() {
        super.onPause();
       // finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        int accountBalance = prefs.getInt("accountBalance",0);
       // tvAccountBalance.setText(String.valueOf(accountBalance) + " RWF");
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedTotal = formatter.format(accountBalance);
        tvAccountBalance.setText(formattedTotal);
    }

    private void sendRegistrationToServer(String token, String merchant_id) {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("merchant_id", merchant_id);
        map.put("token", token);

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, updateFireBaseID, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            //capture response
                            String message = response.get("message").toString();

                            if (message.equalsIgnoreCase("success")) {

                                Toast.makeText(MainMenuActivity.this, "Account Successfully Loaded", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();

                            } else {

                                Toast.makeText(MainMenuActivity.this, "Error Loading Account: " + message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {

                            Toast.makeText(MainMenuActivity.this, "Error Loading Account: ", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainMenuActivity.this, "Error" + error.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }
}
