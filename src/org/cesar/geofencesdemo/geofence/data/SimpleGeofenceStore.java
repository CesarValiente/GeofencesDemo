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

package org.cesar.geofencesdemo.geofence.data;

import java.util.ArrayList;
import java.util.List;

import org.cesar.geofencesdemo.geofence.util.GeofenceUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public class SimpleGeofenceStore {

    private static final String SHARED_PREFERENCES = "GeofencePreferences";
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    public static final String INVALID_STRING_VALUE = "";

    private final SharedPreferences mPrefs;

    public SimpleGeofenceStore(final Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Returns a stored geofence by its id, or returns null if it's not found.
     * 
     * @param id
     *            The ID of a stored geofence (is also the placeId)
     * @return A geofence defined by its center and radius. See
     */
    public SimpleGeofence getGeofence(final String id) {

        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE), INVALID_FLOAT_VALUE);
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE), INVALID_FLOAT_VALUE);
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS), INVALID_FLOAT_VALUE);
        long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                INVALID_LONG_VALUE);
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
                INVALID_INT_VALUE);

        String placeId = mPrefs
                .getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_PLACE_ID), INVALID_STRING_VALUE);
        String country = mPrefs.getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_COUNTRY), INVALID_STRING_VALUE);
        String city = mPrefs.getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_CITY), INVALID_STRING_VALUE);
        String address = mPrefs.getString(getGeofenceFieldKey(id, GeofenceUtils.KEY_ADDRESS), INVALID_STRING_VALUE);

        // If none of the values is incorrect, return the object
        if (lat != INVALID_FLOAT_VALUE && lng != INVALID_FLOAT_VALUE && radius != INVALID_FLOAT_VALUE
                && expirationDuration != INVALID_LONG_VALUE && transitionType != INVALID_INT_VALUE
                && !placeId.equals(INVALID_STRING_VALUE) && !city.equals(INVALID_STRING_VALUE)) {

            return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType, placeId, country,
                    city, address);
            // Otherwise, return null.
        } else {
            return null;
        }
    }

    /**
     * Gets all geofences from the shared preferences
     * 
     * @param numberOfGeofences
     * @return
     */
    public List<SimpleGeofence> getAllGeofences(final int numberOfGeofences) {

        List<SimpleGeofence> geofencesList = new ArrayList<SimpleGeofence>(numberOfGeofences);
        SimpleGeofence item = null;
        for (int i = 0; i < numberOfGeofences; i++) {
            item = getGeofence(String.valueOf(i));
            geofencesList.add(item);
        }
        return geofencesList;
    }

    /**
     * Save a geofence
     * 
     * @param geofence
     *            The SimpleGeofence containing the values you want to save in
     *            SharedPreferences
     */
    public void setGeofence(final String id, final SimpleGeofence geofence) {

        Editor editor = mPrefs.edit();
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE), (float) geofence.getLatitude());
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE), (float) geofence.getLongitude());
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS), geofence.getRadius());
        editor.putLong(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                geofence.getExpirationDuration());
        editor.putInt(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE), geofence.getTransitionType());
        editor.putString(getGeofenceFieldKey(id, GeofenceUtils.KEY_PLACE_ID), geofence.getPlaceId());
        editor.putString(getGeofenceFieldKey(id, GeofenceUtils.KEY_COUNTRY), geofence.getCountry());
        editor.putString(getGeofenceFieldKey(id, GeofenceUtils.KEY_CITY), geofence.getCity());
        editor.putString(getGeofenceFieldKey(id, GeofenceUtils.KEY_ADDRESS), geofence.getAddress());

        editor.commit();
    }

    /**
     * Remove a geofence data given its id
     * 
     * @param id
     */
    public void clearGeofence(final String id) {

        Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_PLACE_ID));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_COUNTRY));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_CITY));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_ADDRESS));

        editor.commit();
    }

    /**
     * Remove the given {@link List} of geofences given their ids
     * 
     * @param geofenceIds
     */
    public void clearGeofence(final List<String> geofenceIds) {

        if (geofenceIds != null) {
            for (String item : geofenceIds) {
                clearGeofence(item);
            }
        }
    }

    /**
     * Given a Geofence object's ID and the name of a field (for example,
     * KEY_LATITUDE), return the key name of the object's values to be used in
     * the SharedPreferences.
     * 
     * @param id
     *            The ID of a Geofence object
     * @param fieldName
     *            The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(final String id, final String fieldName) {
        return GeofenceUtils.KEY_PREFIX + "_" + id + "_" + fieldName;
    }
}
