package com.mvendpay.mvendpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import adapter.HistoryListAdapter;

import appcontroller.AppController;
import model.History;


public class AccountHistoryActivity extends AppCompatActivity {
    private List<History> itemList = new ArrayList<History>();
    private ListView listView;
    private HistoryListAdapter adapter;
    private static final String url = AppController.url_base + "history/";
    String merchant_id;
    ProgressDialog progressDialog;
    private Context mContext;
    TextView tvAccountBalance;
    double inflows = 0;
    double outflows = 0;
    TextView tvInflowLabelAmount;
    TextView tvOutflowLabelAmount;
    TextView tvMerchantName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(AccountHistoryActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("loading...");
        progressDialog.getWindow().setLayout(400,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();
        setContentView(R.layout.activity_transaction_history);
        mContext = this;
        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);
        merchant_id = prefs.getString("merchant_id", null);

        tvAccountBalance = (TextView)findViewById(R.id.tvAccountBalance);
        int accountBalance = prefs.getInt("accountBalance",0);
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedTotal = formatter.format(accountBalance);
        tvAccountBalance.setText(formattedTotal);

        tvMerchantName = (TextView)findViewById(R.id.tvMerchantName);

        String enterprise_name = prefs.getString("enterprise_name",null);
        if(enterprise_name == null || enterprise_name.isEmpty() || enterprise_name.trim().equals("")){
            String fullName    = prefs.getString("fullName",null);
            tvMerchantName.setText(fullName.substring(0, 1).toUpperCase() + fullName.substring(1));
        }else{
            tvMerchantName.setText(enterprise_name.substring(0, 1).toUpperCase() + enterprise_name.substring(1));
        }

        listView = (ListView) findViewById(R.id.tx_transaction_list);

        adapter = new HistoryListAdapter(this, itemList,merchant_id);



        tvInflowLabelAmount = (TextView) findViewById(R.id.tvInflowLabelAmount);


        tvOutflowLabelAmount = (TextView) findViewById(R.id.tvOutflowLabelAmount);


        listView.addFooterView(new View(mContext), null, true);
        listView.addHeaderView(new View(mContext), null, true);
        listView.setAdapter(adapter);

        JsonArrayRequest historyReq = new JsonArrayRequest(url+merchant_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);

                                History hr = new History();
                                hr.setAmount(obj.getString("transaction_amount").trim());
                                hr.setDate(obj.getString("transaction_date").trim());
                                hr.setType(obj.getString("transaction_type").trim());
                                hr.setPayee_id(obj.getString("payee_id").trim());
                                hr.setPayer_id(obj.getString("payer_id").trim());
                                hr.setRequest_ref(obj.getString("request_ref").trim());
                                itemList.add(hr);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        if(response.length() == 0) {
                            Toast.makeText(AccountHistoryActivity.this, "No History to show ", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                        for(History h: itemList){
                            if(h.getType().equals("payment")&&!(h.getPayer_id().equalsIgnoreCase(merchant_id))){
                                inflows += Double.parseDouble(h.getAmount());
                            }else{
                                outflows += Double.parseDouble(h.getAmount());
                            }
                        }
                        DecimalFormat formatter = new DecimalFormat("#,###,###");

                        String formattedTotalInflows = formatter.format(inflows);
                        tvInflowLabelAmount.setText(formattedTotalInflows);

                        String formattedTotalOutflows = formatter.format(outflows);
                        tvOutflowLabelAmount.setText(formattedTotalOutflows);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();

                            }
                        }, 1000);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(historyReq);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
