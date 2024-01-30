package com.opentaxi.opentaxipassenger.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.opentaxi.opentaxipassenger.R;

public class app {

    public static final String TAG = "open taxi";
    public static final String BASE_API_URL = "https://api.opentaxi.ir/";
    public static void log(String message){
        Log.e(TAG,message);
    }

    public static void toast(String message){
        Toast.makeText(application.context, message, Toast.LENGTH_SHORT).show();
    }

    public static void successToast(String message){
        Toast toast = new Toast(application.getContext());
        @SuppressLint("InflateParams") View view = LayoutInflater.from(application.getContext()).inflate(R.layout.success_toast,null);
        TextView txtView = view.findViewById(R.id.textView);
        txtView.setText(message);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.show();
    }

    public static void failToast(String message){
        Toast toast = new Toast(application.getContext());
        @SuppressLint("InflateParams") View view = LayoutInflater.from(application.getContext()).inflate(R.layout.fail_toast,null);
        TextView txtView = view.findViewById(R.id.textView);
        txtView.setText(message);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.show();
    }

    public static boolean isConnectedToNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
