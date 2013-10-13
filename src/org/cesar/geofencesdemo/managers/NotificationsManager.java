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

package org.cesar.geofencesdemo.managers;

import org.cesar.geofencesdemo.R;
import org.cesar.geofencesdemo.geofence.data.SimpleGeofence;
import org.cesar.geofencesdemo.ui.activities.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class NotificationsManager {

    /** Singleton */
    private static NotificationsManager sInstance;

    /** Data Members */
    private Notification mNotification;
    private final NotificationManager mNotificationManager;
    private final Context mContext;

    public static synchronized NotificationsManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new NotificationsManager(context);
        }

        return sInstance;
    }

    public NotificationsManager(final Context ctx) {
        mContext = ctx;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
    }

    /**
     * This notification it has to be launched when we enter or exit from a
     * location we've sepcified before in the {@link MainActivity}
     * 
     * @param geofence
     */
    public void showLocationReminderNotification(final SimpleGeofence geofence) {

        mNotification = new NotificationCompat.Builder(mContext)
                .setTicker(mContext.getString(R.string.location_reminder_label))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mContext.getString(R.string.location_reminder_label))
                .setContentText(geofence.getAddress()).setOnlyAlertOnce(true).setContentIntent(null).build();

        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.defaults |= Notification.DEFAULT_LIGHTS;
        mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotification.defaults |= Notification.DEFAULT_SOUND;

        // Launch notification
        mNotificationManager.notify(Integer.valueOf(geofence.getPlaceId()), mNotification);
    }
}
