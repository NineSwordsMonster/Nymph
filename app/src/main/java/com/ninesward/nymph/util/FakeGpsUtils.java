package com.ninesward.nymph.util;

import android.content.ClipboardManager;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import android.widget.EditText;

import com.ninesward.nymph.model.LocPoint;

public class FakeGpsUtils {
    private static final String TAG = "FakeGpsUtils";

    private FakeGpsUtils() {
    }

    public static void copyToClipboard(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static LocPoint getLocPointFromInput(Context context, EditText editText) {
        LocPoint point = null;
        String text = editText.getText().toString().replace("(", "").replace(")", "");
        String[] split = text.split(",");
        if (split.length == 2) {
            try {
                double lat = Double.parseDouble(split[0].trim());
                double lon = Double.parseDouble(split[1].trim());
                point = new LocPoint(lat, lon);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Parse loc point error!", e);
            }
        }
        return point;
    }

    public static double getMoveStepFromInput(Context context, EditText editText) {
        double step = 0;
        String stepStr = editText.getText().toString().trim();
        try {
            step = Double.valueOf(stepStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parse move step error!", e);
        }

        return step;
    }

    public static int getIntValueFromInput(Context context, EditText editText) {
        int value = 0;
        String stepStr = editText.getText().toString().trim();
        try {
            value = Integer.valueOf(stepStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parse move step error!", e);
        }

        return value;
    }

    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }
}
