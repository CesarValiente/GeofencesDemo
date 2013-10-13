package org.cesar.geofencesdemo.geofence.data;

public class GeofenceLocationDetails {

    private String mCountry;
    private String mCity;
    private String mAddresss;

    /**
     * 
     * @param country
     * @param city
     * @param address
     */
    public GeofenceLocationDetails(final String country, String city,
            final String address) {
        mCountry = country != null ? country : "";
        mCity = city = city != null ? city : "";
        mAddresss = address != null ? address : "";
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCity() {
        return mCity;
    }

    public String getAddress() {
        return mAddresss;
    }

}
