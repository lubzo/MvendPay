package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mvendpay.mvendpay.ApproveMerchantCardActivity;

import appcontroller.Config;
import utils.NotificationUtils;

public class ApproveReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

       // Toast.makeText(context, "Push Start", Toast.LENGTH_LONG).show();

        if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {

        }else  if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {

            // new push notification is received
            String message          =  intent.getStringExtra("message");
            String amount           =  intent.getStringExtra("amount");
            String merchant_buyer   =  intent.getStringExtra("merchant_buyer");
            String merchant_id      =  intent.getStringExtra("merchant_id");
            String narrative        =  intent.getStringExtra("narrative");
            String transactionID    =  intent.getStringExtra("transactionID");

            Intent newIntent = new Intent(context, ApproveMerchantCardActivity.class);

            newIntent.putExtra("message",message);
            newIntent.putExtra("merchant_buyer",merchant_buyer);
            newIntent.putExtra("merchant_id",merchant_id);
            newIntent.putExtra("amount",amount);
            newIntent.putExtra("narrative",narrative);
            newIntent.putExtra("transactionID",transactionID);

            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);

        }else if(intent.getAction().equals(Config.PUSH_NOTIFICATION_RECEIPT)){

            Intent newIntent = new Intent(context, ApproveMerchantCardActivity.class);
            String message          =  intent.getStringExtra("message");
            newIntent.putExtra("message",message);

            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);

           // Toast.makeText(context, "Push notification: Receipt "+ message, Toast.LENGTH_LONG).show();

        }
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(context);
    }
}
