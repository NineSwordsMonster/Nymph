package com.ninesward.nymph.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ninesward.nymph.ILocationManagerImpl;

public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ILocationManagerImpl();
    }
}
