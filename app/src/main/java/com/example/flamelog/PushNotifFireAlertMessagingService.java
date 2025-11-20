package com.example.flamelog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class PushNotifFireAlertMessagingService extends FirebaseMessagingService {

    private static String lastAlertLevel = "SAFE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getNotification() != null ? message.getNotification().getTitle() : "Fire Alert!";
        String body = message.getNotification() != null ? message.getNotification().getBody() : "A fire alert has been triggered.";

        String alertLevel = message.getData().get("alertLevel");

        // will only show the notification for low if the alert level came from safe
        if ("LOW".equalsIgnoreCase(alertLevel)) {
            if ("SAFE".equalsIgnoreCase(lastAlertLevel)) {
                showNotification(title, body, alertLevel);
            } else {
                return;
            }
        } else {
            showNotification(title, body, alertLevel);
        }

        // update alert level
        if (alertLevel != null) {
            lastAlertLevel = alertLevel.toUpperCase();
        }
    }

    private void showNotification(String title, String body, String alertLevel) {
        String channelId = "fire_alerts";

        Uri soundUri;
        long[] vibrationPattern;
        int lightColor;

        if ("LOW".equalsIgnoreCase(alertLevel)) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            vibrationPattern = new long[]{0};
            lightColor = Color.GREEN;
            title = "Low Alert: High Temperature";
            body = "Temperature has exceeded 35°C in site location of SafeFlame hardware. Monitor conditions.";
        } else if ("MEDIUM".equalsIgnoreCase(alertLevel)) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            vibrationPattern = new long[]{0, 300, 300};
            lightColor = Color.YELLOW;
            title = "Medium Alert: Smoke Detected";
            body = "Smoke detected in site location of SafeFlame hardware. Please check immediately.";
        } else if ("HIGH".equalsIgnoreCase(alertLevel)) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            vibrationPattern = new long[]{0, 500, 1000, 500, 1000};
            lightColor = Color.RED;
            title = "High Alert: Flame Detected!";
            body = "Flame detected in site location of SafeFlame hardware! Immediate action required!";
        } else if ("SAFE".equalsIgnoreCase(alertLevel)) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            vibrationPattern = new long[]{0};
            lightColor = Color.BLUE;
            title = "Safe Status";
            body = "Site location is safe. No hazards detected.";
        } else {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            vibrationPattern = new long[]{0, 500};
            lightColor = Color.RED;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Fire Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for fire alerts");
            channel.enableLights(true);
            channel.setLightColor(lightColor);
            channel.enableVibration(true);
            channel.setSound(soundUri, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build());

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(vibrationPattern);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        managerCompat.notify((int) System.currentTimeMillis(), builder.build());
    }
}


