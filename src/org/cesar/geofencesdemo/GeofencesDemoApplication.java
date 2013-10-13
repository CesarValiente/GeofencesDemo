package org.cesar.geofencesdemo;

import android.app.Application;

public class GeofencesDemoApplication extends Application {

    private static GeofencesDemoApplication sInstance;

    public GeofencesDemoApplication() {
        super();
    }

    /**
     * Returns class instance
     * 
     * @return instance
     */
    public synchronized static GeofencesDemoApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        sInstance = this;
    }

}
