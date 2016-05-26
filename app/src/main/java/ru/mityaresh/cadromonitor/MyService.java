package ru.mityaresh.cadromonitor;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            return ! ipAddr.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert an input stream to a buffered reader.
     * @param in An InputStream.
     * @return A BufferedReader or <code>null</code> if something went wrong.
     */
    private static BufferedReader streamToReader(InputStream in) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(in), "UTF-8"), 8);
        } catch (UnsupportedEncodingException e) {
            br = null;
        }

        return br;
    }

    private static String readData(BufferedReader br) throws IOException{
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    private static String httpGet(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("MyService", e.getMessage());
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader = streamToReader(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return readData(reader);
    }

    void sendNotif() {
        if (isNetworkConnected() && isInternetAvailable()) {
            String result = "";
            try {
                result = httpGet("http://cadrspace.ru/status/json");
            } catch (IOException e) {
                Log.e("MyService", e.getMessage());
            }

            Boolean isOpen = false;
            try {
                JSONObject jObject = new JSONObject(result);
                isOpen = jObject.getJSONObject("state").getBoolean("open");
            } catch (JSONException e) {
                Log.e("MyService", e.getMessage());
            }

            Context context = getApplicationContext();
            Intent notificationIntent = null;
            if (isOpen) {
                 notificationIntent = new Intent(context, Snapshot.class);
            } else {
                 notificationIntent = new Intent();
            }

            PendingIntent contentIntent;
            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification.Builder builder = new Notification.Builder(context);

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
