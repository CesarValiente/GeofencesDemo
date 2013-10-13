package org.cesar.geofencesdemo.geofence.callbacks;

import java.util.ArrayList;

import org.cesar.geofencesdemo.geofence.util.GeofenceUtils.AddType;

/**
 * This interface has all callbacks used when we finish our different add,
 * remove or error operations with geofences
 * 
 */
public interface GeofenceCallbacks {

    public void addGeofenceListener(String placeId);

    public void removeGeofenceListener(String placeId, AddType addType);

    public void errorGeofenceListener(String placeId, String message);

    public void showSuggestions(ArrayList<String> suggestions);

}
