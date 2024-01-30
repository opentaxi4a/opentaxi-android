package com.opentaxi.opentaxipassenger.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.opentaxi.opentaxipassenger.R;
import com.opentaxi.opentaxipassenger.app.app;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FrmSpashScreen extends AppCompatActivity {

    private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String languageToLoad  = "fa"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_frm_spash_screen);


        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            public void run() {
                TimerMethod();
            }

        }, 0, 1000);
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private final Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.

            //Do something to the UI thread here
            TextView txtNotInternet = findViewById(R.id.txtNoInternet);
            if(app.isConnectedToNetwork()){
                txtNotInternet.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(FrmSpashScreen.this,FrmLogin.class);
                startActivity(intent);
                myTimer.cancel();
                myTimer.purge();
                myTimer = null;
                finish();
            }
            else{
                txtNotInternet.setVisibility(View.VISIBLE);
            }

        }
    };
}