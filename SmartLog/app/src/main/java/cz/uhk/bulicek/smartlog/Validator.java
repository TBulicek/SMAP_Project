package cz.uhk.bulicek.smartlog;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/**
 * Created by bulicek on 24. 1. 2017.
 */

public class Validator {
    SharedPreferences shprefs;
    Context context;

    public Validator(SharedPreferences shprefs, Context context) {
        this.shprefs = shprefs;
        this.context = context;
    }

    public boolean validateGPS(double lat, double lng) {
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
}
