// ILocationManager.aidl
package com.ninesward.nymph;
import android.location.Location;
// Declare any non-default types here with import statements

interface ILocationManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void reportLocation(in Location location, boolean passive);
}
