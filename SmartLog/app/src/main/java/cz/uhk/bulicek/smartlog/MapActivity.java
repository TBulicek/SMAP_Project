package cz.uhk.bulicek.smartlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private SharedPreferences shprefs;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        shprefs = PreferenceManager.getDefaultSharedPreferences(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                SharedPreferences.Editor editor = shprefs.edit();
                editor.putString("gps_latitude", latLng.latitude + "");
                editor.putString("gps_longitude", latLng.longitude + "");
                editor.commit();
                Toast.makeText(getApplicationContext(),
                        "Latitude set to " + shprefs.getString("gps_latitude", "0.00") + ", \n" +
                        "longitude set to " + shprefs.getString("gps_longitude", "0.00") + ".",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
        // Add a marker in Sydney and move the camera
        double lat = Double.parseDouble(shprefs.getString("gps_latitude", "-34"));
        double lng = Double.parseDouble(shprefs.getString("gps_longitude", "-151"));
        double rad = Double.parseDouble(shprefs.getString("gps_radius", "0"));
        LatLng current = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(current).title("Current position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(current);
        circleOptions.radius(rad);
        mMap.addCircle(circleOptions);
    }
}
