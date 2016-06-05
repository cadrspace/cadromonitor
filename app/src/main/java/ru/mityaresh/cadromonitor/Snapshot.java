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
import java.io.IOException;
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


    Bitmap showPic() {
        Context context = getApplicationContext();
        if (NetUtils.isNetworkConnected(context) && NetUtils.isInternetAvailable()) {
            String result = "";
            try {
                result = NetUtils.httpGet(NetUtils.SPACEAPI_ENDPOINT);
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
                    snap = NetUtils.getCamImage(camUrl);
                }
                return snap;
            } catch (JSONException e) {
                Log.e("Snapshot", e.getMessage());
            }
        }
        return null;
    }
}
