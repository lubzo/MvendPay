package appcontroller;

/**
 * Created by tlubega on 10/4/2017.
 */

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "com.mvendpay.mvendpay.registrationComplete";
    public static final String PUSH_NOTIFICATION = "com.mvendpay.mvendpay.pushNotification";
    public static final String PUSH_NOTIFICATION_RECEIPT = "com.mvendpay.mvendpay.pushNotificationReceipt";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "mvendPay_firebase";
}