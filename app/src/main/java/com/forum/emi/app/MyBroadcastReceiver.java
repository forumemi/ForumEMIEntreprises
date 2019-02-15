package com.forum.emi.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private FirebaseFunctions firebaseFunctions = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String company = intent.getStringExtra("company");
        firebaseFunctions = FirebaseFunctions.getInstance();
        if ("com.forum.emi.app.action.CANCEL_NOTIFICATION".equals(action)) {
            int id = intent.getIntExtra("notification_id", -1);
            if (id != -1) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);
                userResponseTimeOutFunction(company);

            }
        }else if ("com.forum.emi.app.action.VALID_NOTIFICATION".equals(action)){
            // The user hits the confirmation button
            int id = intent.getIntExtra("notification_id", -1);
            if (id != -1) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = MyFirebaseMessagingService.getPendingIntent();
                alarmManager.cancel(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);

            }
        }else if ("com.forum.emi.app.action.INVALID_NOTIFICATION".equals(action)){
            // The user hits the non-confirmation button
            int id = intent.getIntExtra("notification_id", -1);
            if (id != -1) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = MyFirebaseMessagingService.getPendingIntent();
                alarmManager.cancel(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);
                userNegativeResponseFunction(company);
            }
        }
    }
    private Task<String> userResponseTimeOutFunction(String companyID) {
        Map<String ,Object> data = new HashMap<>();
        data.put("company",companyID);
        data.put("push",true);
        return firebaseFunctions
                .getHttpsCallable("userResponseTimeOut")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    private Task<String> userNegativeResponseFunction(String companyID) {
        Map<String ,Object> data = new HashMap<>();
        data.put("company",companyID);
        data.put("push",true);
        return firebaseFunctions
                .getHttpsCallable("userNegativeResponse")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
}
