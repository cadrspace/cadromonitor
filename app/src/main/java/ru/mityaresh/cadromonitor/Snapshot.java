package ru.mityaresh.cadromonitor;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

public class Snapshot extends Activity {

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot);

        mImageView = (ImageView) findViewById(R.id.snapshotfrc);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                GetPic getPicTask = new GetPic();
                getPicTask.execute();
            }
        };

        Timer timer = new Timer();

        timer.schedule(timerTask, 0, 1000);
    }

    class GetPic extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return showPic();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            try {
                mImageView.setImageBitmap(result);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

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

    private static Bitmap getCamImage(String url) {
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
    Bitmap showPic() {
        Context context = getApplicationContext();
        if (NetUtils.isNetworkConnected(context) && NetUtils.isInternetAvailable()) {
            String result = "";
            try {
                result = httpGet(NetUtils.SPACEAPI_ENDPOINT);
            } catch (IOException e) {
                Log.e("Snapshot", e.getMessage());
            }

            Resources res = context.getResources();

            Boolean isOpen = false;
            Bitmap snap = BitmapFactory.decodeResource(res, R.drawable.nosnap);
            try {
                JSONObject jObject = new JSONObject(result);
                isOpen = jObject.getJSONObject("state").getBoolean("open");
                if (isOpen) {
                    String camUrl = jObject.getJSONArray("cam").get(0).toString();
                    snap = getCamImage(camUrl);
                }
                return snap;
            } catch (JSONException e) {
                Log.e("Snapshot", e.getMessage());
            }
        }
        return null;
    }
}
