package com.opentaxi.opentaxipassenger.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentaxi.opentaxipassenger.R;
import com.opentaxi.opentaxipassenger.app.app;
import com.opentaxi.opentaxipassenger.app.application;
import com.opentaxi.opentaxipassenger.app.shareData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FrmSearchingTaxi extends AppCompatActivity {
    RequestQueue requestQueue;

    private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_searching_taxi);

        //set volley request object
        requestQueue = Volley.newRequestQueue(this);

        //timer for check trip status
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            public void run() {
                TimerMethod();
            }

        }, 0, 1000);

    }

    public void onclickCancelTrip(View view){
        new Thread(() -> {
            @SuppressLint({"DefaultLocale", "CommitPrefEdits"}) StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "client/trip/cancel",
                    response -> {
                        app.successToast(getString(R.string.trip_cancelled));
                        shareData.getShareData().edit().putString(shareData.ACTIVE_TRIP,"").apply();
                        Intent intent = new Intent(FrmSearchingTaxi.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    },
                    error -> app.failToast(getString(R.string.network_error))
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", shareData.getShareData().getString(shareData.ACTIVE_TRIP,"0"));
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("X-AUTH-TOKEN", shareData.getShareData().getString(shareData.AUTHKEY,""));
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }).start();
    }

    private void TimerMethod()
    {
        new Thread(() -> {
            @SuppressLint({"DefaultLocale", "CommitPrefEdits"}) StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "client/trip/get",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String errorCode = jsonObject.getString("error");
                                if (errorCode.equals("0")) {
                                    //save number in sharedata
                                    String status = jsonObject.getJSONObject("data").getString("status");
                                    if(status.equals("accepted")){
                                        myTimer.cancel();
                                        Intent intent = new Intent(FrmSearchingTaxi.this,FrmTrip.class);
                                        startActivity(intent);

                                        finish();
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("hrp", "error exude" );
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    error -> app.failToast(getString(R.string.network_error))
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", shareData.getShareData().getString(shareData.ACTIVE_TRIP,"0"));
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("X-AUTH-TOKEN", shareData.getShareData().getString(shareData.AUTHKEY,""));
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }).start();
    }
}