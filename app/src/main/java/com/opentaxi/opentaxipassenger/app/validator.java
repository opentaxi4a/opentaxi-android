package com.opentaxi.opentaxipassenger.app;

public class validator {

    public static boolean mobileNumber(String mobile){
        return mobile.matches("09(1[0-9]|3[1-9]|2[1-9])-?[0-9]{3}-?[0-9]{4}");
    }
}
