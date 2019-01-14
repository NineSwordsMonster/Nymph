package com.ninesward.nymph;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.litesuits.orm.LiteOrm;

public class NymphApp extends Application {

    private static Context sAppContext;

    private static NymphApp app;

    private volatile static LiteOrm sLiteOrm;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d("app : >>>", base.toString());
        sAppContext = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        JoyStickManager.get().init(this);
        sAppContext = getApplicationContext();
        Log.d("app : >>>", sAppContext.toString());
    }

    public static Context get() {
        return app.getApplicationContext();
    }

    public static LiteOrm getLiteOrm() {
        if (sLiteOrm == null) {
            synchronized (NymphApp.class) {
                if (sLiteOrm == null) {
                    sLiteOrm = LiteOrm.newSingleInstance(get(), "fake_gps.db");
                    sLiteOrm.setDebugged(true);
                }
            }
        }
        return sLiteOrm;
    }

}