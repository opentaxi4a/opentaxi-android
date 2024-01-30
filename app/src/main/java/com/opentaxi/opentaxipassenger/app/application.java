package com.opentaxi.opentaxipassenger.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;
import java.util.Locale;

public class application extends Application {

    @SuppressLint("StaticFieldLeak")
    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        getLanguage();
    }

    private void setFonts(String font) {
        Typeface typeface = Typeface.createFromAsset(this.getAssets(),font);
        try {
            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null,typeface);

        }catch (Exception ignore){

        }
    }

    private void getLanguage() {
        String language = Locale.getDefault().getDisplayLanguage();
        String font = "";
        if(language.equalsIgnoreCase("فارسی")){
            font = "fonts/Vazirmatn-Regular.ttf";
        }
        else{
            //english font not dev yet
        }
        if(!font.equals("")){
            setFonts(font);
        }
    }

    public static Context getContext(){
        return context;
    }
}
