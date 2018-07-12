package com.mvendpay.mvendpay;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class TransactionSummaryActivity extends AppCompatActivity {

    private Context mContext;
    private NfcAdapter mNfcAdapter;
    String transactionID;
    String transactionAmount;
    String narrative;
    String merchant_id;
    String uid;
    ProgressDialog progressDialog;
    SharedPreferences.Editor editor;
    String url = AppController.url_base + "processPayment";
    private int accountBalance;
    private int transactionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_summary);

        mContext = this;
        //Retrieve adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        //collect data from intent
        transactionID = getIntent().getStringExtra("transactionID");
        transactionAmount = getIntent().getStringExtra("TouchPayAmount");
        narrative = getIntent().getStringExtra("TouchPayNarrative");
        merchant_id = getIntent().getStringExtra("merchant_id");

        SharedPreferences prefs = getSharedPreferences("mvendPay", MODE_PRIVATE);

        accountBalance = prefs.getInt("accountBalance", 0);
        transactionCount = prefs.getInt("transactionCount", 0);

        TextView tvTransactionID = (TextView) findViewById(R.id.tvTransactionID);
        tvTransactionID.setText(transactionID);

        TextView tvTransactionAmount = (TextView) findViewById(R.id.tvTransactionAmount);
        tvTransactionAmount.setText(transactionAmount);

        TextView tvTransactionNarrative = (TextView) findViewById(R.id.tvTransactionNarrative);
        tvTransactionNarrative.setText(narrative);

        /*Button btnTransactionSummaryTapCard = (Button) findViewById(R.id.btnTransactionSummaryTapCard);

        btnTransactionSummaryTapCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReceipt = new Intent(getBaseContext(), TransactionReceiptActivity.class);
                startActivity(intentReceipt);
            }
        });*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        progressDialog = new ProgressDialog(TransactionSummaryActivity.this,
                R.style.AppTheme_White_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing Payment...");
        progressDialog.show();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


            byte[] tagIdbyteArray = tag.getId();

            uid = ByteArrayToHexString(tagIdbyteArray);

            processCard(uid, merchant_id, transactionAmount, transactionID, narrative);

        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


            byte[] tagIdbyteArray = tag.getId();

            uid = ByteArrayToHexString(tagIdbyteArray);

            processCard(uid, merchant_id, transactionAmount, transactionID, narrative);

        } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


            byte[] tagIdbyteArray = tag.getId();

            uid = ByteArrayToHexString(tagIdbyteArray);

            processCard(uid, merchant_id, transactionAmount, transactionID, narrative);

        }

    }

    private void processCard(String uid, String merchantID, final String transactionAmount, final String transactionID, final String narrative) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("merchant_id", merchantID);
        map.put("uid", uid);
        map.put("transactionID", transactionID);
        map.put("transactionAmount", transactionAmount);
        map.put("narrative", narrative);
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

                                String type = response.get("type").toString();

                                if(type.equalsIgnoreCase("notification")) {

                                    String notification = response.getString("notification");
                                    progressDialog.dismiss();
                                    Toast.makeText(TransactionSummaryActivity.this, notification, Toast.LENGTH_LONG).show();
                                    Intent mainMenuIntent = new Intent(mContext,MainMenuActivity.class);
                                    startActivity(mainMenuIntent);
                                    finish();
                                    return;

                                }


                                Toast.makeText(TransactionSummaryActivity.this, message, Toast.LENGTH_LONG).show();
                                accountBalance = accountBalance + Integer.parseInt(transactionAmount);
                                progressDialog.dismiss();
                                editor = getSharedPreferences("mvendPay", MODE_PRIVATE).edit();
                                editor.putInt("accountBalance", accountBalance);
                                editor.commit();

                                String paymentRef = response.get("paymentRef").toString();
                                String receiptNumber = response.get("receiptNo").toString();
                                Intent intentReceipt = new Intent(getBaseContext(), TransactionReceiptActivity.class);
                                intentReceipt.putExtra("transactionID", transactionID);
                                intentReceipt.putExtra("transactionAmount", transactionAmount);
                                intentReceipt.putExtra("narrative", narrative);
                                intentReceipt.putExtra("paymentRef", paymentRef);
                                intentReceipt.putExtra("receiptNumber", receiptNumber);
                                startActivity(intentReceipt);

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(TransactionSummaryActivity.this, message, Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(TransactionSummaryActivity.this, "error: " + error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
        AppController.getInstance().addToRequestQueue(jsonRequest);

    }


    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        // Notice that this is the same filter as in our manifest.
        IntentFilter tag_tech = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tag_tech.addCategory(Intent.CATEGORY_DEFAULT);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }


        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        tech.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            tech.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        IntentFilter[] filters = new IntentFilter[]{tag_tech, ndef, tech};

        String[][] techList = new String[][]{new String[]{NfcA.class.getName(),
                NfcB.class.getName(), NfcF.class.getName(),
                NfcV.class.getName(), IsoDep.class.getName(),
                MifareClassic.class.getName(),
                MifareUltralight.class.getName(), NdefFormatable.class.getName(), Ndef.class.getName()}};

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link } requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);


    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();

        // finish();

    }

}
