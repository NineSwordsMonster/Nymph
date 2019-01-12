package com.ninesward.nymph.util;

import android.app.Activity;
import android.content.pm.PackageManager;

public class PermissionUtil {
    /**
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     * @param grantResults
     * @return
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
