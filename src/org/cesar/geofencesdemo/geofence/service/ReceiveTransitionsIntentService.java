/*
 * Copyright (C) 2006 The Android Open Source Project
 * 
 * Modifications and additions by: Cesar Valiente 
 * mail: cesar.valiente@gmail.com
 * twitter: @CesarValiente
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
