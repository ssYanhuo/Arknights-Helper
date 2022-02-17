package com.ssyanhuo.arknightshelper.misc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.MainActivity;
import com.ssyanhuo.arknightshelper.service.BroadcastReceiver;

/**
 * Helper class for showing and canceling service
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class ServiceNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Service";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of service notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     */
    public static Notification build(final Context context,
                                     final int number) {
        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).

        final String title = res.getString(
                R.string.notification_title);
        final String text = res.getString(
                R.string.notification_text);
        final String ticker = res.getString(
                R.string.notification_ticker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel("notification", context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW));
        }
        Intent stopServiceIntent = new Intent(context, BroadcastReceiver.class).setAction("com.ssyanhuo.arknightshelper.stopservice");
        stopServiceIntent.putExtra("action", "StopService");
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 1, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 1, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Intent resumeIntent = new Intent(context, MainActivity.class);
        resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            builder = new NotificationCompat.Builder(context, "notification")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setTicker(ticker)
                    .setNumber(number)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(text)
                            .setBigContentTitle(title)
                            .setSummaryText(ticker))
                    .addAction(
                            R.drawable.ic_nofication_home,
                            res.getString(R.string.notification_action_home),
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    resumeIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        } else {
            builder = new NotificationCompat.Builder(context, "notification")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setTicker(ticker)
                    .setNumber(number)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(text)
                            .setBigContentTitle(title)
                            .setSummaryText(ticker))
                    .addAction(
                            R.drawable.ic_nofication_home,
                            res.getString(R.string.notification_action_home),
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    resumeIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT));
        }

        return builder.build();
    }

}
