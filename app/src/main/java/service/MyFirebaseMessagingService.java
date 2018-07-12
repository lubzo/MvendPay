package service;

/**
 * Created by tlubega on 10/4/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mvendpay.mvendpay.ApproveMerchantCardActivity;
import com.mvendpay.mvendpay.MainMenuActivity;

import org.json.JSONException;
import org.json.JSONObject;

import appcontroller.Config;
import utils.NotificationUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
            }
        }else{

        }
    }

    private void handleNotification(String message) {

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

            if(message.contains("ReceiptNo")){
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION_RECEIPT);
                pushNotification.putExtra("message", message);
                sendBroadcast(pushNotification);

            }else{

                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
               //notificationUtils.playNotificationSound();
            }

        }else{
            // If the app is in background, firebase itself handles the notification

        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.d(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");

            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            String amount     =  data.getString("amount");
            String merchant_buyer = data.getString("merchant_buyer");
            String merchant_id    = data.getString("merchant_id");
            String narrative    = data.getString("narrative");
            String transactionID    = data.getString("transactionID");

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message + " " + amount);
                pushNotification.putExtra("amount", amount);
                pushNotification.putExtra("merchant_buyer", merchant_buyer);
                pushNotification.putExtra("merchant_id", merchant_id);
                pushNotification.putExtra("narrative", narrative);
                pushNotification.putExtra("transactionID", transactionID);
                sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                notificationUtils.playNotificationSound();
            } else {

                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), ApproveMerchantCardActivity.class);
                resultIntent.putExtra("message", message + " " + amount);
                resultIntent.putExtra("amount", amount);
                resultIntent.putExtra("merchant_buyer", merchant_buyer);
                resultIntent.putExtra("merchant_id", merchant_id);
                resultIntent.putExtra("narrative", narrative);
                resultIntent.putExtra("transactionID", transactionID);
                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {

        } catch (Exception e) {

        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
