package cz.uhk.bulicek.smartlog;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by bulicek on 24. 1. 2017.
 */

public class Validator {
    SharedPreferences shprefs;
    Context context;
    LocationManager mLocationManager;

    public Validator(Context context) {
        this.shprefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public boolean validateGPS() {
        Location loc = getLastKnownLocation();
        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        double latPref = Double.parseDouble(shprefs.getString("gps_latitude", "0"));
        double lngPref = Double.parseDouble(shprefs.getString("gps_longitude", "0"));
        double radPref = Double.parseDouble(shprefs.getString("gps_radius", "0"));

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(latPref - lat);
        Double lonDistance = Math.toRadians(lngPref - lng);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(latPref))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return (distance <= radPref);
    }

    public boolean validateWiFi() {
        boolean validity = false;
        Networker nw = new Networker(context);
        String wifiPref = shprefs.getString("wifi_ssid", "");
        int mode = Integer.parseInt(shprefs.getString("wifi_mode", "1"));
        if (mode == 1) {
            List<String> availableNetworks = nw.getAvailibleNetworks();
            validity = availableNetworks.contains(wifiPref);
        } else {
            String currentSSID = nw.getLocalInfo()[0];
            validity = wifiPref.equals(currentSSID);
        }
        return validity;
    }

    public boolean validateMAC() {
        Networker nw = new Networker(context);
        String defIp = shprefs.getString("ip_address", "");
        String MAC = shprefs.getString("mac_address", "");
        String currIP = nw.getLocalInfo()[1];

        return nw.findMACOnNetwork(defIp, currIP, MAC);
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
