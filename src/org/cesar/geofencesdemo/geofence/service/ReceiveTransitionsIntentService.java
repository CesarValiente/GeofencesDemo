package org.cesar.geofencesdemo.geofence.service;

import java.util.List;

import org.cesar.geofencesdemo.geofence.data.SimpleGeofence;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofenceStore;
import org.cesar.geofencesdemo.geofence.util.CommonUtils;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils;
import org.cesar.geofencesdemo.geofence.util.LocationServiceErrorMessages;
import org.cesar.geofencesdemo.managers.NotificationsManager;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveTransitionsIntentService extends IntentService {

    private final String LOG_TAG = ReceiveTransitionsIntentService.class.getSimpleName();

    private String mPlaceId;

    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    protected boolean isCorrectIntent(final Intent intent) {
        if (intent != null) {
            mPlaceId = intent.getStringExtra(GeofenceUtils.KEY_PLACE_ID);
            return true;
        }
        return false;
    }

    /**
     * Handles incoming intents
     * 
     * @param intent
     *            The Intent sent by Location Services. This Intent is provided
     *            to Location Services (inside a PendingIntent) when you call
     *            addGeofences()
     */
    @Override
    protected void onHandleIntent(final Intent intent) {

        if (isCorrectIntent(intent)) {

            if (LocationClient.hasError(intent)) {

                int errorCode = LocationClient.getErrorCode(intent);

                // Get the error message
                String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
                Log.d(LOG_TAG, "Error: " + errorMessage);

                CommonUtils.showShortToast(this, errorMessage);
            } else {
                // Get the type of transition (entry or exit)
                int transition = LocationClient.getGeofenceTransition(intent);

                // Test that a valid transition was reported (just an enter
                // transition, uncomment the commented
                // code to allow too transition exit
                if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER)
                /* || (transition == Geofence.GEOFENCE_TRANSITION_EXIT) */) {

                    List<Geofence> geofenceList = LocationClient.getTriggeringGeofences(intent);
                    String[] geofenceIds = new String[geofenceList.size()];

                    // We can remove this loop since we are using just one id
                    // (with more this is necessary)
                    for (int i = 0; i < geofenceIds.length; i++) {
                        String placeId = geofenceList.get(i).getRequestId();
                        if (placeId.equals(mPlaceId)) {
                            geofenceIds[i] = placeId;
                            Log.d(LOG_TAG, "geofence ID received: " + placeId);
                            SimpleGeofence geofence = new SimpleGeofenceStore(this).getGeofence(geofenceIds[i]);
                            if (geofence != null) {
                                NotificationsManager.getInstance(this).showLocationReminderNotification(geofence);
                            }
                        }
                    }
                }
                // An invalid transition was reported
                else {
                    Log.e(LOG_TAG, "Geofence transition error: " + Integer.toString(transition));
                }
            }
        }
    }
}
