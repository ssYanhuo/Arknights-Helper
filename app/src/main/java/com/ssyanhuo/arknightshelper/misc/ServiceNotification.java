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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resumeIntent = new Intent(context, MainActivity.class);
        resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Set ticker text (preview) information for this notification.
                .setTicker(ticker)

                // Show a number. This is useful when stacking notifications of
                // a single type.
                .setNumber(number)

                // If this notification relates to a past or upcoming event, you
                // should set the relevant time information using the setWhen
                // method below. If this call is omitted, the notification's
                // timestamp will by set to the time at which it was shown.
                // TODO: Call setWhen if this notification relates to a past or
                // upcoming event. The sole argument to this method should be
                // the notification timestamp in milliseconds.
                //.setWhen(...)

                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(pendingIntent)

                // Show expanded text content on devices running Android 4.1 or
                // later.
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText(ticker))

                // Example additional actions for this notification. These will
                // only show on devices running Android 4.1 or later, so you
                // should ensure that the activity in this notification's
                // content intent provides access to the same actions in
                // another way.
                .addAction(
                        R.drawable.ic_nofication_home,
                        res.getString(R.string.notification_action_home),
                        PendingIntent.getActivity(
                                context,
                                0,
                                resumeIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setChannelId("notification");

        return builder.build();
    }

}
