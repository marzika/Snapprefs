package com.marz.snapprefs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marz.snapprefs.Util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends Activity {
    static final LatLng orlando = new LatLng(28.377144, -81.570611);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        final List<Marker> markerList = new ArrayList<>();
        Marker orlandodisney = map.addMarker(new MarkerOptions().position(orlando)
                .title("Disneyland - Orlando"));
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                FileUtils.writeToSDFile(String.valueOf(latLng.latitude), "latitude");
                FileUtils.writeToSDFile(String.valueOf(latLng.longitude), "longitude");
                Toast.makeText(MapsActivity.this, "Spoofing location for " + latLng.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
