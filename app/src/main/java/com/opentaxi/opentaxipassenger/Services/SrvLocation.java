package com.opentaxi.opentaxipassenger.Services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.opentaxi.opentaxipassenger.app.app;


public class SrvLocation extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        //set location callback

    }

    @Override
    public IBinder onBind(Intent intent) {
        app.successToast("ddddd");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

