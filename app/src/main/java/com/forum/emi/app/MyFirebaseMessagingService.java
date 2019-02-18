package com.forum.emi.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static PendingIntent pi;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private DatabaseReference databaseRef = null;
    private Map<String,String> payload = null;
    private String task = null;
    private String CHANNEL_ID = "1234";
    private Uri alarmSound = null;
    private NotificationManager manager ;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            // Get payload from remote message
            payload = remoteMessage.getData();
            task = payload.get("TASK");


            if(task.equals("notification")) {
                // Handle Notifications
                String flag = remoteMessage.getData().get("FLAG");
                if (flag.equals("0")){
                    Intent notificationIntentInvalid = new Intent(this, MyBroadcastReceiver.class);
                    notificationIntentInvalid.putExtra("company", remoteMessage.getData().get("company"));
                    notificationIntentInvalid.setAction("com.forum.emi.app.action.INVALID_NOTIFICATION");
                    notificationIntentInvalid.putExtra("notification_id", 123);
                    PendingIntent contentIntentInvalid = PendingIntent.getBroadcast(this,
                            0, notificationIntentInvalid,
                            PendingIntent.FLAG_CANCEL_CURRENT);

                    Intent notificationIntentValid = new Intent(this, MyBroadcastReceiver.class);
                    notificationIntentValid.putExtra("company", remoteMessage.getData().get("company"));
                    notificationIntentValid.setAction("com.forum.emi.app.action.VALID_NOTIFICATION");
                    notificationIntentValid.putExtra("notification_id", 123);
                    PendingIntent contentIntentValid = PendingIntent.getBroadcast(this,0, notificationIntentValid,PendingIntent.FLAG_CANCEL_CURRENT);

                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

                    // TODO : fix notification icons red color
                    // TODO : fix sound for android pie

                    Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                            .addAction(R.drawable.ic_check_circle_black_24dp,"Confirmer",contentIntentValid)
                            .addAction(R.drawable.ic_remove_circle_black_24dp,"Abondonner",contentIntentInvalid)
                            .setOngoing(true)
                            .setFullScreenIntent(null,true)
                            .setAutoCancel(true)
                            .setContentTitle(remoteMessage.getData().get("title"))
                            .setContentText(remoteMessage.getData().get("body"))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setSound(alarmSound)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                            .build();
                    manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    createNotificationChannel();
                    manager.notify(123, notification);

                    AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                    Intent cancelNotificationIntent = new Intent(this,MyBroadcastReceiver.class);
                    cancelNotificationIntent.putExtra("company", remoteMessage.getData().get("company"));
                    cancelNotificationIntent.setAction("com.forum.emi.app.action.CANCEL_NOTIFICATION");
                    cancelNotificationIntent.putExtra("notification_id", 123);
                    pi = PendingIntent.getBroadcast(this, 0, cancelNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,Calendar.getInstance().getTimeInMillis()+15*1000,pi);
                } else {
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                            .setFullScreenIntent(null,true)
                            .setAutoCancel(true)
                            .setContentTitle(remoteMessage.getData().get("title"))
                            .setContentText(remoteMessage.getData().get("body"))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setSound(alarmSound)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                            .build();
                    manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    createNotificationChannel();
                    manager.notify(123, notification);
                }

            }
        }
        if (remoteMessage.getNotification() != null) {
            Notification notification = new NotificationCompat.Builder(this,"1234")
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            createNotificationChannel();
            manager.notify(123, notification);
        }


    }

    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        if (databaseRef != null){
            databaseRef.child("Users").child(firebaseUser.getUid()).child("token").setValue(token);
        }
    }
    // [END on_new_token]

    /**
     * This is used in order to retrive the PendingIntent from MyBroadcastReceiver
     * to handle responding to notification ( VALID / INVALID )
     */
    public static PendingIntent getPendingIntent(){
        return pi;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(alarmSound,audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            manager.createNotificationChannel(channel);
        }
    }

}