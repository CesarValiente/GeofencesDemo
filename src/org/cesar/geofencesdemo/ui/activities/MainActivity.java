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

package org.cesar.geofencesdemo.ui.activities;

import java.util.ArrayList;
import java.util.List;

import org.cesar.geofencesdemo.R;
import org.cesar.geofencesdemo.geofence.actions.GeofenceRemover;
import org.cesar.geofencesdemo.geofence.actions.GeofenceRequester;
import org.cesar.geofencesdemo.geofence.callbacks.GeofenceCallbacks;
import org.cesar.geofencesdemo.geofence.data.GeofenceLocationDetails;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofence;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofenceStore;
import org.cesar.geofencesdemo.geofence.util.CommonUtils;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils.AddType;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils.RemoveType;
import org.cesar.geofencesdemo.geofence.util.GeofenceUtils.RequestType;
import org.cesar.geofencesdemo.map.MapSearchAutocompletion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements OnCameraChangeListener, GeofenceCallbacks,
        OnItemClickListener {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    /*
     * Use to set an expiration time for a geofence. After this amount of time
     * Location Services will stop tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    private static final float GEOFENCE_RADIUS = 50.0f;

    private RequestType mRequestType;
    private RemoveType mRemoveType;

    private List<Geofence> mGeofenceList;
    private List<String> mGeofencesToRemove;
    private SimpleGeofence mSimplegeofence;
    private ArrayAdapter<String> mAutocompleteAdapter;

    private SimpleGeofenceStore mGeofenceStorage;

    // Request and remove handlers
    private GeofenceRequester mGeofenceRequester;
    private GeofenceRemover mGeofenceRemover;

    // Gets the most recently position
    private Double mLatitudeNow;
    private Double mLongitudeNow;
    private GeofenceLocationDetails mLocationDetailsNow;

    private AutoCompleteTextView mAutocompleteText;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mCountry;
    private TextView mCity;
    private TextView mAddress;

    private ActionBar mActionBar;

    // Gogole map object we use to get all things we need
    private GoogleMap mGoogleMap;

    // This is the id used to add geofences in this demo, to use this code on a
    // much more versatil way
    // we have to change this id, and for instance use this as the objects id we
    // have to retrieve info
    // when the transition happens
    private final int PLACE_ID = 1;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        mActionBar = getSupportActionBar();

        mAutocompleteText = (AutoCompleteTextView) findViewById(R.id.map_autocomplete_textview);
        mLatitudeText = (TextView) findViewById(R.id.map_latitude_text);
        mLongitudeText = (TextView) findViewById(R.id.map_longitude_text);
        mCountry = (TextView) findViewById(R.id.map_country_text);
        mCity = (TextView) findViewById(R.id.map_city_text);
        mAddress = (TextView) findViewById(R.id.map_address_text);

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(getBaseContext());

        mGeofenceList = new ArrayList<Geofence>(1);
        mGeofencesToRemove = new ArrayList<String>(1);

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(getBaseContext(), this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(getBaseContext(), this);

        // Autocomplete stuff
        mAutocompleteAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item);
        mAutocompleteText.setAdapter(mAutocompleteAdapter);
        mAutocompleteText.setOnItemClickListener(this);

        mAutocompleteText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                setProgressBarIndeterminateVisibility(true);
                new MapSearchAutocompletion(s.toString(), MainActivity.this).execute();
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(final Editable s) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                // If we had another geofence previously, we have to remove it
                if (mSimplegeofence != null) {
                    removeGeofences(AddType.ADD_AFTER_REMOVE);
                } else if (mLatitudeNow != null && mLongitudeNow != null) {
                    createGeofences();
                }
                return true;
            case R.id.action_delete:
                if (mGeofencesToRemove.size() > 0) {
                    removeGeofences(AddType.NONE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void showSuggestions(final ArrayList<String> suggestions) {

        if (mAutocompleteAdapter != null && mAutocompleteAdapter.getCount() > 0) {
            mAutocompleteAdapter.clear();
        }
        mAutocompleteAdapter.addAll(suggestions);
        mAutocompleteAdapter.notifyDataSetChanged();
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Address address = GeofenceUtils.getReverseLocationDetails(this, str);
        if (address != null) {
            CommonUtils.hideSoftKeyboard(this, mAutocompleteText);
            updateCamera(address.getLatitude(), address.getLongitude());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSimplegeofence = mGeofenceStorage.getGeofence(String.valueOf(PLACE_ID));
        if (mSimplegeofence != null) {
            mGeofenceList.add(0, mSimplegeofence.toGeofence());
            mGeofencesToRemove.add(0, String.valueOf(PLACE_ID));
        }
        setUpMapIfNeeded();
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed()
     * in GeofenceRemover and GeofenceRequester may call
     * startResolutionForResult() to start an Activity that handles Google Play
     * services problems. The result of this call returns here, to
     * onActivityResult. calls
     */

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

        // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.RequestType.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current
                            // geofences
                            mGeofenceRequester.addGeofences(mGeofenceList, String.valueOf(PLACE_ID));

                            // If the request was to remove geofences
                        } else if (GeofenceUtils.RequestType.REMOVE == mRequestType) {

                            // Toggle the removal flag and send a new removal
                            // request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.RemoveType.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the
                                // PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester
                                        .getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofencesToRemove, AddType.NONE,
                                        String.valueOf(PLACE_ID));
                            }
                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to
                        // resolve the problem.
                        Log.d(LOG_TAG, "google play was unable to resolve the problem");
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LOG_TAG, "An unknown activity result code has been received");

                break;
        }
    }

    // -------------------- Check google Play services is available
    // ----------------------//

    /**
     * Verify that Google Play services is available before making a request.
     * 
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(LOG_TAG, "Google play services available");
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LOG_TAG);
            }
            return false;
        }
    }

    // ------------------- Add geofences --------------------------------//

    /**
     * Get the geofence parameters for each geofence from the UI and add them to
     * a List.
     */
    private void createGeofences() {

        mSimplegeofence = new SimpleGeofence(String.valueOf(PLACE_ID), mLatitudeNow, mLongitudeNow,
                GEOFENCE_RADIUS, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER,
                String.valueOf(PLACE_ID), mLocationDetailsNow.getCountry(), mLocationDetailsNow.getCity(),
                mLocationDetailsNow.getAddress());

        // Then save the new one
        mGeofenceStorage.setGeofence(String.valueOf(PLACE_ID), mSimplegeofence);
        mGeofenceList.add(0, mSimplegeofence.toGeofence());
        mGeofencesToRemove.add(0, String.valueOf(PLACE_ID));

        /*
         * Record the request as an ADD. If a connection error occurs, the app
         * can automatically restart the add request if Google Play services can
         * fix the error
         */
        mRequestType = GeofenceUtils.RequestType.ADD;

        /*
         * Check for Google Play services. Do this after setting the request
         * type. If connecting to Google Play services fails, onActivityResult
         * is eventually called, and it needs to know what type of request was
         * in progress.
         */
        if (!servicesConnected()) {
            return;
        }

        // Start the request. Fail if there's already a request in progress
        try {
            mGeofenceRequester.addGeofences(mGeofenceList, String.valueOf(PLACE_ID));
        } catch (UnsupportedOperationException e) {
            CommonUtils.showShortToast(getBaseContext(), R.string.connection_previous_request_not_finished);
        }
    }

    // ------------------- Remove geofences ---------------------------------//

    /**
     * This is the method called when we want to remove a geofence
     */
    private void removeGeofences(final AddType addType) {

        /*
         * Record the removal as remove by Intent. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        // Record the type of removal, if you want to use the intent removal,
        // change the RemoveType used
        mRemoveType = GeofenceUtils.RemoveType.LIST;

        /*
         * Check for Google Play services. Do this after setting the request
         * type. If connecting to Google Play services fails, onActivityResult
         * is eventually called, and it needs to know what type of request was
         * in progress.
         */
        if (!servicesConnected()) {
            return;
        }
        // Try to make a removal request
        try {
            /*
             * Remove the geofences represented by the currently-active
             * PendingIntent. If the PendingIntent was removed for some reason,
             * re-create it; since it's always created with FLAG_UPDATE_CURRENT,
             * an identical PendingIntent is always created.
             */
            mGeofenceRemover.removeGeofencesById(mGeofencesToRemove, addType, String.valueOf(PLACE_ID));

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.connection_previous_request_not_finished, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void addGeofenceListener(final String placeId) {

        if (placeId.equals(String.valueOf(PLACE_ID))) {
            Log.d(LOG_TAG, "Added new geofence: " + placeId);
            CommonUtils.showShortToast(this, R.string.geofence_added);
        }
    }

    @Override
    public void removeGeofenceListener(final String placeId, final AddType addType) {
        if (placeId.equals(String.valueOf(PLACE_ID))) {
            Log.d(LOG_TAG, "Removed received. PlaceID: " + placeId);
            mGeofenceStorage.clearGeofence(mGeofencesToRemove);
            mGeofenceList.clear();
            mGeofencesToRemove.clear();
            mSimplegeofence = null;
            if (addType == AddType.ADD_AFTER_REMOVE) {
                createGeofences();
            } else {
                CommonUtils.showShortToast(this, R.string.geofence_deleted);
            }
        }
    }

    @Override
    public void errorGeofenceListener(final String placeId, final String message) {
        Log.d(LOG_TAG, "Error received. PlaceId: " + placeId + "\nMessage: " + message);
        CommonUtils.showShortToast(this, message);
    }

    /**
     * 
     * Class to launch a error dialog fragment
     * 
     */
    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(final Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            return mDialog;
        }
    }

    // ------------------ Maps ----------------------------------------//

    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mGoogleMap == null) {
            mGoogleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mGoogleMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.setOnCameraChangeListener(this);

                LatLng latLng = null;
                // If we have a previous location set, we go there
                if (mSimplegeofence != null) {
                    latLng = new LatLng(mSimplegeofence.getLatitude(), mSimplegeofence.getLongitude());
                } else {
                    // If we don't have a previous location set, we try to go to
                    // the last known position, if it's
                    // not possible, then we go to the 0.0, 0.0 location
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String bestProvider = locationManager.getBestProvider(new Criteria(), true);
                    Location location = locationManager.getLastKnownLocation(bestProvider);
                    if (location != null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    } else {
                        latLng = new LatLng(0, 0);
                    }
                }
                updateCamera(latLng);
            }
        }
    }

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {

        LatLng latlng = cameraPosition.target;
        mLatitudeNow = latlng.latitude;
        mLongitudeNow = latlng.longitude;
        mLatitudeText.setText("latitude: " + mLatitudeNow);
        mLongitudeText.setText("longitude: " + mLongitudeNow);

        mLocationDetailsNow = GeofenceUtils.getLocationDetails(this, latlng.latitude, latlng.longitude);
        if (mLocationDetailsNow != null) {
            mCountry.setText("Country: " + mLocationDetailsNow.getCountry());
            mCity.setText("City: " + mLocationDetailsNow.getCity());
            mAddress.setText("Address: " + mLocationDetailsNow.getAddress());
        }
    }

    private void updateCamera(final double latitude, final double longitude) {

        LatLng latLng = new LatLng(latitude, longitude);
        updateCamera(latLng);
    }

    private void updateCamera(final LatLng latLng) {
        if (latLng != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

}
