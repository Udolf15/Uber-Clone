package com.asifhashmi.uber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {
    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
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
        RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.relLayout);
        relativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LatLng driverLatLng=new LatLng(getIntent().getDoubleExtra("driverLat",0),getIntent().getDoubleExtra("driverLon",0));
                LatLng requestLatLng=new LatLng(getIntent().getDoubleExtra("reqLat",0),getIntent().getDoubleExtra("reqLon",0));

                ArrayList<Marker> markers=new ArrayList<Marker>();


                LatLngBounds.Builder builder=new LatLngBounds.Builder();

                markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Rider Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));


                for(Marker marker:markers){
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds=builder.build();

                int padding=25;

                CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,padding);

                mMap.moveCamera(cu);

                mMap.animateCamera(cu);


            }
        });
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }



    public void acceptReq(View view){
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");

        query.whereEqualTo("username",getIntent().getStringExtra("username"));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for(ParseObject driverDetails:objects){
                            //   updateMap(userLastKnownLocation);
                            driverDetails.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            driverDetails.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){

                                        Intent intent=new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+getIntent().getDoubleExtra("driverLat",0)+","+getIntent().getDoubleExtra("driverLon",0)+"&daddr="+getIntent().getDoubleExtra("reqLat",0)+","+getIntent().getDoubleExtra("reqLon",0)));
                                        startActivity(intent);
                                        Toast.makeText(DriverMapActivity.this, "Accepted Request", Toast.LENGTH_SHORT).show();
                                        }else{
                                        Log.i("Error",e.getMessage());
                                        }
                                }
                                });
                            }

                          }else{
                            Log.i("Error bro","none");
                        }
                    }else{
                        Log.i("Error bro",e.getMessage());
                    }
                };
            });
    }
}
