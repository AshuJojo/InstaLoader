package com.trickdarinda.instaloader.services;
/*
    Created By Jobless Joker on 05-08-2020 at 07:26 PM
*/

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.trickdarinda.instaloader.R;
import com.trickdarinda.instaloader.activities.MainActivity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClipboardJobService extends JobService {
    private static final String TAG = "ClipboardJobService";
    private static final String CHANNEL_ID = "clipboard_copy_event";
    private static final String CHANNEL_NAME = "Clipboard Copy Event";
    private static final int NOTIFICATION_ID = 2342;
    private NotificationManager notificationManager;
    private ClipboardManager clipboardManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        Log.e(TAG, "onStartJob: Service Started");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "onStopJob: Service finished");
        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setName("Copy Link");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setContentTitle("InstaLoader");
        notificationBuilder.setContentText("Tap to Download");
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        //Intent to back to MainActivity when the notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        notificationBuilder.setContentIntent(pendingIntent);

        return notificationBuilder;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    String url = (Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getText()).toString().trim();
                    if (checkUrl(url)) {
                        //Create notification channel
                        createNotificationChannel();
                        //Create Notification builder
                        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
                        //Notify to the notification manager
                        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
                        Log.e(TAG, "onPrimaryClipChanged: ");
                    }
                }
            };

    /* method to validate url */
    private boolean checkUrl(String url) {
        Log.e(TAG, "checkUrl: url = " + url);
        //REGULAR EXPRESSION for validation of url
        String REGEX = "https://www.instagram.com/(p|reel|tv)/(.*?)/(.*?)";
        //pattern to check the url with REGULAR EXPRESSION
        Pattern postsUrlPattern = Pattern.compile(REGEX);
        //check the url with the help of matcher with pattern
        Matcher matcher = postsUrlPattern.matcher(url);

        //return if the pattern matches or not
        return matcher.matches();
    }
}
