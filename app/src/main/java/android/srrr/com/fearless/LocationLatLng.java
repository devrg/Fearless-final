package android.srrr.com.fearless;

public class LocationLatLng {
    private double lat, lng;

    public LocationLatLng(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public LocationLatLng(){

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        String latLngStr = "";
        latLngStr += "["+lat+","+lng+"]";
        return latLngStr;
    }
}
