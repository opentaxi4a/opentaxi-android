package com.opentaxi.opentaxipassenger.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.opentaxi.opentaxipassenger.app.shareData;
import com.opentaxi.opentaxipassenger.app.validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FrmLogin extends AppCompatActivity {

    EditText    txtMobile;
    TextView lblErrorMobile;
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_login);
        init();
    }
    private void init(){
        requestQueue = Volley.newRequestQueue(this);
        txtMobile = findViewById(R.id.txtMobile);
        lblErrorMobile = findViewById(R.id.editText_error_mobile);
        String apikey = shareData.getShareData().getString(shareData.AUTHKEY,null);
        if(apikey != null){
            Intent frmMain = new Intent(FrmLogin.this, MainActivity.class);
            startActivity(frmMain);
            finish();
        }
    }
    @SuppressLint("CommitPrefEdits")
    public void onclickBtnLogin(View v){
        Button btnSubmit = v.findViewById(R.id.btn_sendcode);
        btnSubmit.setText(getString(R.string.in_progress));
        String mobile = txtMobile.getText().toString().trim();
        if(!validator.mobileNumber(mobile)){
            lblErrorMobile.setVisibility(View.VISIBLE);
        }
        else {
            //going to send active code
            lblErrorMobile.setVisibility(View.INVISIBLE);
            new Thread(new Runnable() {
                public void run() {
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            app.BASE_API_URL + "user/login",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        String errorCode = jsonObject.getString("error");
                                        String message = jsonObject.getString("message");
                                        if (errorCode.equals("0")) {
                                            //save number in sharedata
                                            shareData.getShareData().edit().putString(shareData.MOBILE, mobile).apply();
                                            //open accept code form
                                            Intent intent = new Intent(FrmLogin.this, FrmInputActiveCode.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (errorCode.equals("103")) {
                                            btnSubmit.setText(getString(R.string.btnSendCode));
                                            app.failToast(getString(R.string.too_many_request));
                                        } else {
                                            btnSubmit.setText(getString(R.string.btnSendCode));
                                            app.failToast(message);
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    app.failToast(getString(R.string.network_error));
                                }
                            }

                    ) {
                        @NonNull
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("mobile", mobile);
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
            }).start();
        }
    }
}