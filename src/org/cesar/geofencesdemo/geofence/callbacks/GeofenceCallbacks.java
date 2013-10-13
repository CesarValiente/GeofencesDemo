/*
 *   Copyright (2013) Cesar Valiente 
 *      mail: cesar.valiente@gmail.com
 *      twitter: @CesarValiente
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
