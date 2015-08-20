package com.marz.snapprefs;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marz.snapprefs.Util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends Activity {
    static final LatLng orlando = new LatLng(28.377144, -81.570611);
    MarkerOptions markerOptions;
    LatLng latLng;
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

        // Getting reference to btn_find of the layout activity_main
        Button btn_find = (Button) findViewById(R.id.btn_find);

        // Defining button click event listener for the find button
        OnClickListener findClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.et_location);

                // Getting user input location
                String location = etLocation.getText().toString();

                if (location != null && !location.equals("")) {
                    new GeocoderTask().execute(location);
                }
            }
        };

        // Setting button click event listener for the find button
        btn_find.setOnClickListener(findClickListener);
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
            // Adding Markers on Google Map for each matching address
            Address address = (Address) addresses.get(0);

            // Creating an instance of GeoPoint, to display in Google Map
            latLng = new LatLng(address.getLatitude(), address.getLongitude());

            String addressText = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(addressText);

            map.addMarker(markerOptions);

            // Locate the first location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4));
        }
    }
}
