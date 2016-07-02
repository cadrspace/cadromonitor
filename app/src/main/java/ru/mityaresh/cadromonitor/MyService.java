package ru.mityaresh.cadromonitor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;


public class MyService extends Service {
    NotificationManager nm;

    private static final int POLL_TIMEOUT = 15000; // ms

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNotif();
            }
        }, 0, POLL_TIMEOUT);

        return START_STICKY;
    }

    void sendNotif() {
        Context context = getApplicationContext();
        if (NetUtils.isNetworkConnected(context) && NetUtils.isInternetAvailable() || true) {
            String result = "";
            try {
                result = NetUtils.httpGet(NetUtils.SPACEAPI_ENDPOINT);
            } catch (IOException e) {
                Log.e("MyService", e.getMessage());
            }

            Boolean isOpen = false;
            try {
                JSONObject jObject = new JSONObject(result);
                isOpen = jObject.getJSONObject("state").getBoolean("open");
            } catch (JSONException e) {
                Log.e("Error JSON", e.getMessage());
            }


            Intent notificationIntent = null;
            if (isOpen) {
                 notificationIntent = new Intent(context, Snapshot.class);
            } else {
                 notificationIntent = new Intent();
            }

            PendingIntent contentIntent;
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.nf_cadr)
                    .setTicker(getString(R.string.notyTitle) + " " + getString(isOpen ? R.string.open : R.string.close))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentTitle(getString(R.string.notyTitle))
                    .setContentText(getString(isOpen ? R.string.open : R.string.close));

            Notification notification = builder.build();


            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(101, notification);
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
