// NetUtils.java -- Network utilites.

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
import java.net.URLDecoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;

class NetUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return ! ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }
    public static final String SPACEAPI_ENDPOINT =
            "https://cadrspace.ru/status/json";

    private static BufferedReader streamToReader(InputStream in) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(in), "UTF-8"), 8);
        } catch (UnsupportedEncodingException e) {
            br = null;
        }

        return br;
    }

    private static String readData(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    public static String httpGet(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("Snapshot", e.getMessage());
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

    public static Bitmap getCamImage(String url) {
        try {
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            decodedUrl = decodedUrl.substring(0, 1 + decodedUrl.indexOf('=')) + "snapshot";
            return BitmapFactory.decodeStream((InputStream) new URL(decodedUrl).getContent());
        } catch (MalformedURLException e) {
            Log.e("Snapshot", e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e("Snapshot", e.getMessage());
        } catch (IOException e) {
            Log.e("Snapshot", e.getMessage());
        }
        return null;
    }
}
