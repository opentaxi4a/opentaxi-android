package com.opentaxi.opentaxipassenger.views;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentaxi.opentaxipassenger.app.app;
import com.opentaxi.opentaxipassenger.app.shareData;

import com.opentaxi.opentaxipassenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FrmInputActiveCode extends AppCompatActivity {

    RequestQueue requestQueue;
    EditText codeView;
    String mobileNumber;
    private void init(){
        requestQueue = Volley.newRequestQueue(this);
        codeView = findViewById(R.id.txtPincode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_input_active_code);
        TextView yourNumberPhone = findViewById(R.id.txtYourNumber);
        mobileNumber = shareData.getShareData().getString(shareData.MOBILE,"");
        yourNumberPhone.setText(getString(R.string.your_phone_number,mobileNumber));
        init();
    }

    public void onclickBackBtn(View view){
        Intent intent = new Intent(FrmInputActiveCode.this,FrmLogin.class);
        startActivity(intent);
        finish();
    }

    public void onclickBtnLogin(View view) {

        String pincode = codeView.getText().toString().trim();

        if(pincode.length() != 6){
            app.failToast(getString(R.string.code_wrong));
            codeView.setText("");
        }
        else {
            //going to send active code
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    app.BASE_API_URL + "user/auth",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String errorCode = jsonObject.getString("error");
                                String message = jsonObject.getString("message");
                                String appData = jsonObject.getString("data");
                                if (errorCode.equals("0")) {
                                    //save number in sharedata
                                    shareData.getShareData().edit().putString(shareData.AUTHKEY, appData).apply();
                                    //open accept code form
                                    Intent intent = new Intent(FrmInputActiveCode.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (errorCode.equals("103")) {
                                    app.failToast(getString(R.string.too_many_request));
                                } else {
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
                    params.put("mobile", mobileNumber);
                    params.put("code", pincode);
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }
    }
}