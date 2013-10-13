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

package org.cesar.geofencesdemo.geofence.actions;

import java.util.Arrays;
import java.util.List;

import org.cesar.geofencesdemo.R;
import org.cesar.geofencesdemo.geofence.callbacks.GeofenceCallbacks;
import org.cesar.geofencesdemo.geofence.service.ReceiveTransitionsIntentService;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

/**
 * Class for connecting to Location Services and requesting geofences. <b> Note:
 * Clients must ensure that Google Play services is available before requesting
 * geofences. </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to
 * check.
 * 
 * 
 * To use a GeofenceRequester, instantiate it and call AddGeofence(). Everything
 * else is done automatically.
 * 
 */
public class GeofenceRequester implements OnAddGeofencesResultListener, ConnectionCallbacks,
        OnConnectionFailedListener {

    private final String LOG_TAG = GeofenceRequester.class.getSimpleName();

    private String mPlaceId;

    private final Context mContext;
    private GeofenceCallbacks mListener;

    // Stores the PendingIntent used to send geofence transitions back to the
    // app
    private PendingIntent mGeofencePendingIntent;

    // Stores the current list of geofences
    private List<Geofence> mCurrentGeofences;

    // Stores the current instantiation of the location client
    private LocationClient mLocationClient;

    /*
     * Flag that indicates whether an add or remove request is underway. Check
     * this flag before attempting to start a new request.
     */
    private boolean mInProgress;

    /**
     * Default private constructor
     * 
     * @param context
     */
    public GeofenceRequester(final Context context, final GeofenceCallbacks listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * Set the "in progress" flag from a caller. This allows callers to re-set a
     * request that failed but was later fixed.
     * 
     * @param flag
     *            Turn the in progress flag on or off.
     */
    public void setInProgressFlag(final boolean flag) {
        mInProgress = flag;
    }

    /**
     * Get the current in progress status.
     * 
     * @return The current value of the in progress flag.
     */
    public boolean getInProgressFlag() {
        return mInProgress;
    }

    /**
     * Returns the current PendingIntent to the caller.
     * 
     * @return The PendingIntent used to create the current set of geofences
     */
    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or
     * onConnectionFailure() is called.
     */
    private void requestConnection() {
        getLocationClient().connect();
    }

    /**
     * Get the current location client, or create a new one if necessary.
     * 
     * @return A LocationClient object
     */
    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(mContext, this, this);
        }
        return mLocationClient;

    }

    /**
     * Start adding geofences. Save the geofences, then start adding them by
     * requesting a connection
     * 
     * @param geofences
     *            A List of one or more geofences to add
     */
    public void addGeofences(final List<Geofence> geofences, final String placeId)
            throws UnsupportedOperationException {

        /*
         * Save the geofences so that they can be sent to Location Services once
         * the connection is available.
         */
        mCurrentGeofences = geofences;
        mPlaceId = placeId;

        // If a request is not already in progress
        if (!mInProgress) {

            // Toggle the flag and continue
            mInProgress = true;

            // Request a connection to Location Services
            requestConnection();

            // If a request is in progress
        } else {

            // Throw an exception and stop the request
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Once the connection is available, send a request to add the Geofences
     */
    private void continueAddGeofences() {

        // Get a PendingIntent that Location Services issues when a geofence
        // transition occurs
        mGeofencePendingIntent = createRequestPendingIntent();

        // Send a request to add the current geofences
        mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);
    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed If a
     * connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

        // Turn off the request flag
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {

            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult((FragmentActivity) mContext,
                        GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            mListener.errorGeofenceListener(mPlaceId, mContext.getString(R.string.geofences_add_fails));
        }
    }

    /*
     * Called by Location Services once the location client is connected.
     * 
     * Continue by adding the requested geofences.
     */
    @Override
    public void onConnected(final Bundle arg0) {

        Log.d(LOG_TAG, "Connected");

        // Continue adding the geofences
        continueAddGeofences();
    }

    /*
     * Called by Location Services once the location client is disconnected.
     */
    @Override
    public void onDisconnected() {

        // Turn off the request flag
        mInProgress = false;

        Log.d(LOG_TAG, "Disconnected");

        // Destroy the current location client
        mLocationClient = null;
    }

    /**
     * Get a location client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // A request is no longer in progress
        mInProgress = false;

        getLocationClient().disconnect();
    }

    /*
     * Handle the result of adding the geofences
     */
    @Override
    public void onAddGeofencesResult(final int statusCode, final String[] geofenceRequestIds) {

        if (mListener != null) {
            if (LocationStatusCodes.SUCCESS == statusCode) {
                Log.d(LOG_TAG, "Geofences ids added: " + Arrays.toString(geofenceRequestIds));

                mListener.addGeofenceListener(mPlaceId);
            } else {
                Log.d(LOG_TAG, "Add geofences failed. Ids: " + Arrays.toString(geofenceRequestIds));

                mListener.errorGeofenceListener(mPlaceId, mContext.getString(R.string.geofences_add_fails));
            }
        }
        // Disconnect the location client
        requestDisconnection();
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     * 
     * @return A PendingIntent for the IntentService that handles geofence
     *         transitions.
     */
    private PendingIntent createRequestPendingIntent() {

        if (mGeofencePendingIntent == null) {

            Intent intent = new Intent(mContext, ReceiveTransitionsIntentService.class);
            intent.putExtra(GeofenceUtils.KEY_PLACE_ID, mPlaceId);
            /*
             * Return a PendingIntent to start the IntentService. Always create
             * a PendingIntent sent to Location Services with
             * FLAG_UPDATE_CURRENT, so that sending the PendingIntent again
             * updates the original. Otherwise, Location Services can't match
             * the PendingIntent to requests made with it.
             */
            mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
    }
}
