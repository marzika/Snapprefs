package com.marz.snapprefs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Activity {
    static final LatLng orlando = new LatLng(28.377144, -81.570611);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker orlandodisney = map.addMarker(new MarkerOptions().position(orlando)
                .title("Disneyland - Orlando"));
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                if (arg0.getTitle().equals("Disneyland - Orlando")) // if marker source is clicked
                    // Move the camera instantly to hamburg with a zoom of 15.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(orlando, 15));

                // Zoom in, animating the camera.
                map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                return true;
            }

        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(MapsActivity.this, "Spoofing location for " + latLng.toString(), Toast.LENGTH_SHORT).show(); //do some stuff
            }
        });
    }
}
