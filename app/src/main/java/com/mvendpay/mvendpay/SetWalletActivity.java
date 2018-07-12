package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import adapter.WalletListAdapter;
import appcontroller.AppController;
import model.Wallet;

public class SetWalletActivity extends AppCompatActivity {
    private List<Wallet> itemList = new ArrayList<Wallet>();
    private ListView listView;
    private WalletListAdapter adapter;
    private Context mContext;
    private int buffKey = 0;
    String merchant_id;
    boolean hasSetPin = false;
    String url = AppController.url_base + "setPin";
    String url2 = AppController.url_base + "addNewWallet";
    String url3 = AppController.url_base + "wallets/";
    String url4 = AppController.url_base + "setWalletStatus";
    AlertDialog alertDialog;
    AlertDialog alertDialog1;
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    String operatorNameString;
    String phoneNumberString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallet);
        mContext = this;
        editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);
        hasSetPin = prefs.getBoolean("hasSetPin", false);

        if (!hasSetPin) {
            firstTimePin();
        }
        listView = (ListView) findViewById(R.id.wallet_list);


        adapter = new WalletListAdapter(this, itemList);
        listView.addFooterView(new View(mContext), null, true);
        listView.addHeaderView(new View(mContext), null, true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tvWalletID = (TextView) view.findViewById(R.id.walletItemID);
                TextView tvPhoneNumber = (TextView) view.findViewById(R.id.walletPhoneNumber);
                String walletID = tvWalletID.getText().toString().trim();
                String phoneNumber = tvPhoneNumber.getText().toString().trim();
                showDialogWalletOptions(walletID,phoneNumber);


            }
        });

        loadWallets();
    }

    //prompt user to set pin if accessing for the first time
    private void firstTimePin() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.set_pin, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AppTheme_White_Dialog);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText newPin = (EditText) promptsView.findViewById(R.id.etSetPin);
        final EditText newConfirmPin = (EditText) promptsView.findViewById(R.id.etSetPinConfirm);


        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null);
        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        String newPinString = newPin.getText().toString().trim();
                        String newConfirmPinString = newConfirmPin.getText().toString().trim();

                        if (!validate(newPinString, newConfirmPinString, newPin, newConfirmPin)) {
                            Toast.makeText(mContext, "Error with input", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        progressDialog = new ProgressDialog(SetWalletActivity.this,
                                R.style.AppTheme_White_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("loading...");
                        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progressDialog.show();
                        setPin(newPinString);


                    }
                });
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        dialog.dismiss();

                        Intent intentMainMenu = new Intent(mContext, MainMenuActivity.class);
                        startActivity(intentMainMenu);

                        finish();
                    }
                });
            }
        });


        TextView msg = new TextView(this);
        msg.setText("SET YOUR NEW PIN");
        msg.setTextSize(18);
        msg.setPadding(5, 5, 5, 5);
        //msg.setTextColor();
        msg.setGravity(Gravity.CENTER);
        alertDialog.setCustomTitle(msg);
        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private boolean validate(String newPin, String newConfirmPin, EditText etNewPin, EditText etNewConfirmPin) {
        boolean valid = true;

        if (newPin.isEmpty() || newPin.length() < 4) {
            etNewPin.setError("Pin must have 4 or more digits");
            valid = false;
        } else {
            etNewPin.setError(null);
        }

        if (newConfirmPin.isEmpty() || newConfirmPin.length() < 4) {
            etNewConfirmPin.setError("Confirm Pin must have 4 or more digits");
            valid = false;
        } else {
            etNewConfirmPin.setError(null);
        }

        if (!newPin.equalsIgnoreCase(newConfirmPin)) {
            etNewConfirmPin.setError("Pin Numbers do not match");
            valid = false;
        } else {
            etNewConfirmPin.setError(null);
        }

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plus_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_item) {

            promptForAddWallet();

            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void promptForAddWallet() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.add_wallet, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AppTheme_White_Dialog);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText operatorName = (EditText) promptsView.findViewById(R.id.etSetOperator);
        final EditText phoneNumber = (EditText) promptsView.findViewById(R.id.etSetPhoneNumber);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null);

        // create alert dialog
        final AlertDialog alertDialog2 = alertDialogBuilder.create();
        // show it
        alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        operatorNameString = operatorName.getText().toString().trim();
                        phoneNumberString = phoneNumber.getText().toString().trim();

                        if (!validate1(operatorNameString, phoneNumberString, operatorName, phoneNumber)) {
                            Toast.makeText(mContext, "Error with input", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        alertDialog2.dismiss();
                        promptForPin();

                    }
                });
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        alertDialog2.dismiss();
                    }
                });
            }
        });
        TextView msg = new TextView(this);
        msg.setText("ADD WALLET");
        msg.setTextSize(18);
        msg.setPadding(5, 5, 5, 5);
        //msg.setTextColor();
        msg.setGravity(Gravity.CENTER);
        alertDialog2.setCustomTitle(msg);
        alertDialog2.show();
        alertDialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private boolean validate1(String operatorNameString, String phoneNumberString, EditText etOperatorName, EditText etPhoneNumber) {
        boolean valid = true;
        if (operatorNameString.isEmpty() || operatorNameString.length() == 0) {
            etOperatorName.setError("Operator Name cannot be empty");
            valid = false;
        } else {
            etOperatorName.setError(null);
        }

        if (phoneNumberString.isEmpty() || phoneNumberString.length() == 0) {
            etPhoneNumber.setError("Phone Number cannot be empty");
            valid = false;
        } else {
            etPhoneNumber.setError(null);
        }


        return valid;
    }

    private void promptForPin() {
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
        alertDialog1 = alertDialogBuilder.create();
        alertDialog1.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something


                        String pin = etPin.getText().toString().trim();
                        if (!validate2(pin, etPin)) {
                            Toast.makeText(mContext, "Error with input", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        progressDialog = new ProgressDialog(SetWalletActivity.this,
                                R.style.AppTheme_White_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("loading...");
                        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progressDialog.show();
                        submitNewWallet(operatorNameString, phoneNumberString, pin);

                    }
                });
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        dialog.dismiss();
                        operatorNameString = "";
                        phoneNumberString = "";
                    }
                });
            }
        });
        // show it
        TextView msg = new TextView(this);
        msg.setText("Confirm Wallet Addition");
        msg.setTextSize(18);
        msg.setPadding(5, 5, 5, 5);
        //msg.setTextColor();
        msg.setGravity(Gravity.CENTER);
        alertDialog1.setCustomTitle(msg);
        alertDialog1.show();
        alertDialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void submitNewWallet(String operatorNameString, String phoneNumberString, String pin) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("pin", pin);
        map.put("operatorName", operatorNameString);
        map.put("phoneNumber", phoneNumberString);
        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, url2, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                Toast.makeText(SetWalletActivity.this, "Wallet Added Successfully", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                alertDialog1.dismiss();
                                loadWallets();
                            } else {

                                Toast.makeText(SetWalletActivity.this, "Error,Please try again", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SetWalletActivity.this, "Server Error ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);

    }

    private boolean validate2(String Pin, EditText etPin) {
        boolean valid = true;
        if (Pin.isEmpty() || Pin.length() == 0) {
            etPin.setError("Pin cannot be empty");
            valid = false;
        } else {
            etPin.setError(null);
        }

        return valid;
    }

    private void showDialogWalletOptions(final String walletRowID,final String phoneNumber) {

        final CharSequence[] choiceArray = {"Collection Wallet", "Default Payment Wallet", "Remove Wallet"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_White_Dialog);
        // Set the dialog title
        builder.setTitle("Select Option for Wallet");
        final int selected = -1; // does not select anything

        builder.setSingleChoiceItems(
                choiceArray,
                selected,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        buffKey = which;
                    }
                })
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //String option = choiceArray[buffKey].toString();
                        String option = String.valueOf(buffKey);
                        progressDialog = new ProgressDialog(SetWalletActivity.this,
                                R.style.AppTheme_White_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("loading...");
                        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                        progressDialog.show();
                        setWalletStatus(option,walletRowID,phoneNumber);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(
                                mContext,
                                "Cancelled",
                                Toast.LENGTH_SHORT
                        )
                                .show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setWalletStatus(String option, String walletRowID,final String phoneNumber) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("option", option);
        map.put("walletRowID", walletRowID);
        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, url4, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //capture response
                            String message = response.get("message").toString();
                            if (message.equalsIgnoreCase("success")) {
                                itemList.clear();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(SetWalletActivity.this, "Wallet "+ phoneNumber +" Updated Successfully", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                loadWallets();
                                progressDialog.dismiss();


                            } else {
                                Toast.makeText(SetWalletActivity.this, "Error,Please try again", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SetWalletActivity.this, "Server Error ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void setPin(String pin) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchant_id);
        map.put("pin", pin);
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
                                Toast.makeText(SetWalletActivity.this, "Pin Updated Successfully", Toast.LENGTH_LONG).show();
                                editor.putBoolean("hasSetPin", true);
                                editor.commit();
                                progressDialog.dismiss();
                                alertDialog.dismiss();

                            } else {
                                Toast.makeText(SetWalletActivity.this, "Error,Please try again", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SetWalletActivity.this, "Server Error ", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void loadWallets() {

        progressDialog = new ProgressDialog(SetWalletActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("loading...");
        progressDialog.getWindow().setLayout(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();

        JsonArrayRequest jsonArrayReq = new JsonArrayRequest(url3 + merchant_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        itemList.clear();
                        adapter.notifyDataSetChanged();
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Wallet walletPref = new Wallet();
                                walletPref.setWalletItemID(obj.getString("record_id").trim());
                                walletPref.setPhoneNumber(obj.getString("msisdn").trim());
                                walletPref.setNetworkName(obj.getString("telco_provider_name").trim());
                                walletPref.setStatus(obj.getString("link_type").trim());

                                itemList.add(walletPref);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        if (response.length() == 0) {

                            Toast.makeText(SetWalletActivity.this, "No Available Wallets ", Toast.LENGTH_SHORT).show();

                        }

                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(SetWalletActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayReq);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}


