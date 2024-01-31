package com.opentaxi.opentaxipassenger.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigInteger;

public class shareData {
    static SharedPreferences shareData;

    public static SharedPreferences getShareData(){
        if(shareData == null){
            shareData = application.getContext().getSharedPreferences(app.TAG, Context.MODE_PRIVATE);
        }
        return shareData;
    }

    public static final String MOBILE = "";

    public static final String LAST_CALC_ID = "LastCalcID";

    public static final String ACTIVE_TRIP = "activeTrip";

    public static final String AUTHKEY = "AUTH-KEY";
    public static final String LANGUAGE = "fa";
    public static final String USERNAME = "USERNAME";

    public static final String P1LAT = "P1LAT";
    public static final String P2LAT = "P2LAT";
    public static final String P3LAT = "P3LAT";
    public static final String P1LONG = "P1LONG";
    public static final String P2LONG = "P2LONG";
    public static final String P3LONG = "P3LONG";

}
