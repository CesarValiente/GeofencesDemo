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

package org.cesar.geofencesdemo.geofence.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.cesar.geofencesdemo.R;
import org.cesar.geofencesdemo.geofence.data.GeofenceLocationDetails;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofence;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofenceStore;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.Geofence;

/**
 * This class defines constants used by location sample apps.
 */
public final class GeofenceUtils {

    // Used to track what type of geofence removal request was made.
    public enum RemoveType {
        INTENT, LIST
    }

    // Used to track what type of request is in process
    public enum RequestType {
        ADD, REMOVE
    }

    public enum AddType {
        ADD_AFTER_REMOVE, NONE
    }

    // Intent actions
    public static final String ACTION_GEOFENCE_ERROR = "org.cesar.geofencesdemo.ACTION_GEOFENCES_ERROR";
    public static final String ACTION_GEOFENCE_TRANSITION = "org.cesar.geofencesdemo.ACTION_GEOFENCE_TRANSITION";
    public static final String ACTION_GEOFENCE_TRANSITION_ERROR = "org.cesar.geofencesdemo.ACTION_GEOFENCE_TRANSITION_ERROR";

    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES = "org.cesar.geofencesdemo.CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE = "org.cesar.geofencesdemo.EXTRA_CONNECTION_CODE";
    public static final String EXTRA_CONNECTION_ERROR_CODE = "org.cesar.geofencesdemo.EXTRA_CONNECTION_ERROR_CODE";
    public static final String EXTRA_CONNECTION_ERROR_MESSAGE = "org.cesar.geofencesdemo.EXTRA_CONNECTION_ERROR_MESSAGE";
    public static final String EXTRA_GEOFENCE_STATUS = "org.cesar.geofencesdemo.EXTRA_GEOFENCE_STATUS";

    /*
     * Keys for flattened geofences stored in SharedPreferences
     */
    public static final String KEY_LATITUDE = "org.cesar.geofencesdemo.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "org.cesar.geofencesdemo.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "org.cesar.geofencesdemo.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "org.cesar.geofencesdemo.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "org.cesar.geofencesdemo.KEY_TRANSITION_TYPE";
    public static final String KEY_NUMBER_GEOFENCES = "org.cesar.geofencesdemo.KEY_NUMBER_GEOFENCES";
    public static final String KEY_PLACE_ID = "org.cesar.geofencesdemo.KEY_PLACE_ID";
    public static final String KEY_COUNTRY = "org.cesar.geofencesdemo.KEY_COUNTRY";
    public static final String KEY_CITY = "org.cesar.geofencesdemo.KEY_CITY";
    public static final String KEY_ADDRESS = "org.cesar.geofencesdemo.KEY_ADDRESS";

    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX = "org.cesar.geofencesdemo.KEY";

    public static final String REQ_GEOFENCE_ADDED = "reqGeofenceAdded";
    public static final String REQ_GEOFENCE_DELETED = "reqGeofenceDeleted";
    public static final String REQ_COUNTRY = "reqCountry";
    public static final String REQ_CITY = "reqCity";
    public static final String REQ_ADDRESS = "reqAddress";

    public static final String ADD_TYPE = "addType";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // A string of length 0, used to clear out input fields
    public static final String EMPTY_STRING = new String();

    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * 
     * @param transitionType
     *            A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    public static String getTransitionString(final Context context, final int transitionType) {

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.transition_in);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.transition_out);

            default:
                return context.getString(R.string.transition_unknown);
        }
    }

    /**
     * Gets the {@link GeofenceLocationDetails} associated to the given data
     * 
     * @param context
     * @param latitude
     * @param longitude
     * @return
     */
    public static GeofenceLocationDetails getLocationDetails(final Context context, final double latitude,
            final double longitude) {

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getAddressLine(1);
                    String country = addresses.get(0).getAddressLine(2);
                    return new GeofenceLocationDetails(country, city, address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Given a {@link String} location returns an {@link Address}
     * 
     * @param context
     * @param location
     * @return
     */
    public static Address getReverseLocationDetails(final Context context, final String location) {

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(location, 1);
                if (addresses != null && addresses.size() == 1) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Returns a {@link SimpleGeofence} if we have one associated to the given
     * placeId
     * 
     * @param context
     * @param placeId
     * @return
     */
    public static SimpleGeofence isSimpleGeofenceAssociated(final Context context, final String placeId) {

        SimpleGeofenceStore store = new SimpleGeofenceStore(context);
        return store.getGeofence(placeId);
    }
}
