package org.cesar.geofencesdemo.geofence.data;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its multiple attrs.
 */
public class SimpleGeofence {

    private String mId;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;
    private String mPlaceId;
    private String mCountry;
    private String mCity;
    private String mAddress;

    /**
     * @param Id
     *            The id of the geofence
     * @param latitude
     *            Latitude of the Geofence's center.
     * @param longitude
     *            Longitude of the Geofence's center.
     * @param radius
     *            Radius of the geofence circle.
     * @param expiration
     *            Geofence expiration duration
     * @param transition
     *            Type of Geofence transition.
     * @param placeId
     *            id of the place is linked to
     * @param country
     * @param city
     * @param address
     * 
     */
    public SimpleGeofence(final String id, final double latitude, final double longitude, final float radius,
            final long expiration, final int transition, final String placeId, final String country,
            final String city, final String address) {

        mId = id;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mExpirationDuration = expiration;
        mTransitionType = transition;
        mPlaceId = placeId;
        mCountry = country;
        mCity = city;
        mAddress = address;
    }

    public String getId() {
        return mId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public float getRadius() {
        return mRadius;
    }

    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    public int getTransitionType() {
        return mTransitionType;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCity() {
        return mCity;
    }

    public String getAddress() {
        return mAddress;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * 
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder().setRequestId(getId()).setTransitionTypes(mTransitionType)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration).build();
    }

}
