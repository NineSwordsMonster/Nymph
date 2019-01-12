package com.ninesward.nymph;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.ninesward.nymph.ui.JoyStickManager;

import java.io.File;

public class NymphApp extends Application {

    private static Context sAppContext;
    private volatile static LiteOrm sLiteOrm;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sAppContext = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
        JoyStickManager.get().init(this);
    }

    public static Context get() {
        return sAppContext;
    }

    public static LiteOrm getLiteOrm() {
        if (sLiteOrm == null) {
            synchronized (NymphApp.class) {
                if (sLiteOrm == null) {
                    sLiteOrm = LiteOrm.newSingleInstance(sAppContext, "fake_gps.db");
                    sLiteOrm.setDebugged(true);
                }
            }
        }
        return sLiteOrm;
    }

    private void initLog() {
        String outputDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fakegps";
        File file = new File(outputDir);
        if (!file.isDirectory()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}