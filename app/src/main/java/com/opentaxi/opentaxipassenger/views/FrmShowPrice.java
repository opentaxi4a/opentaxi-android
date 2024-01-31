package com.opentaxi.opentaxipassenger.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

public class FrmShowPrice extends AppCompatActivity {

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_show_price);

        //set volley request object
        requestQueue = Volley.newRequestQueue(this);
        //show address
        new Thread(() -> {
            @SuppressLint("DefaultLocale") StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "client/location/get-address",
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errorCode = jsonObject.getString("error");
                            if (errorCode.equals("0")) {
                                //save number in sharedata
                                String price = jsonObject.getJSONObject("data").getString("price");
                                TextView textview = findViewById(R.id.priceButton);
                                textview.setText(price);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error -> app.failToast(getString(R.string.network_error))
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("lat", shareData.getShareData().getString(shareData.P1LAT,"0"));
                    params.put("long", shareData.getShareData().getString(shareData.P1LONG,"0"));
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
        //calculate price
        new Thread(() -> {
            @SuppressLint("DefaultLocale") StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "client/trip/calc/get",
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errorCode = jsonObject.getString("error");
                            if (errorCode.equals("0")) {
                                //save number in sharedata
                                String price = jsonObject.getJSONObject("data").getString("price");
                                TextView textview = findViewById(R.id.priceButton);
                                textview.setText(price);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error -> app.failToast(getString(R.string.network_error))
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", shareData.getShareData().getString(shareData.LAST_CALC_ID,"0"));
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

    public void btnSubmitTrip(View view){
        new Thread(() -> {
            @SuppressLint({"DefaultLocale", "CommitPrefEdits"}) StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "client/trip/request",
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errorCode = jsonObject.getString("error");
                            if (errorCode.equals("0")) {
                                //save number in sharedata
                                shareData.getShareData().edit().putString(shareData.ACTIVE_TRIP,jsonObject.getJSONObject("data").getString("tripID")).apply();
                                Intent intent = new Intent(FrmShowPrice.this,FrmSearchingTaxi.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error -> app.failToast(getString(R.string.network_error))
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", shareData.getShareData().getString(shareData.LAST_CALC_ID,"0"));
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