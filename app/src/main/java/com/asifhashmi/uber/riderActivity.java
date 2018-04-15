package com.asifhashmi.uber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class riderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean isCalling=false;
    Handler handler=new Handler();
    TextView driverDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button callOrCancelB=(Button)findViewById(R.id.callOrCancelB);
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Request");

        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0) {
                        isCalling = true;
                        callOrCancelB.setText("Cancel Uber");

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkForUpdates();
                            }
                        }, 2000);
                    }
                }
            }
        });

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

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        locationManager =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location userLastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(userLastKnownLocation!=null){
                updateMap(userLastKnownLocation);
            }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults.length>0){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location userLastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(userLastKnownLocation!=null){
                        updateMap(userLastKnownLocation);
                    }
                }
            }
        }
    }

    public void updateMap(Location location){

        if(!isCalling) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
        }
    }

    public void logout(View view){
        ParseUser.logOut();
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    public void callOrCancel(View view){
        ParseObject request=new ParseObject("Request");
        Button callOrCancelB=(Button)findViewById(R.id.callOrCancelB);

        if(!isCalling) {
            callOrCancelB.setText("Cancel Uber");
            isCalling=true;

            ParseGeoPoint geoPoint;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location userLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (userLastKnownLocation != null) {
                    request.put("username", ParseUser.getCurrentUser().getUsername());

                    geoPoint = new ParseGeoPoint(userLastKnownLocation.getLatitude(), userLastKnownLocation.getLongitude());

                    request.put("location", geoPoint);

                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                checkForUpdates();
                                Toast.makeText(riderActivity.this, "Uber Requested", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(riderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }else{
            callOrCancelB.setText("Call Uber");
            isCalling=false;

            ParseQuery<ParseObject> user=new ParseQuery<ParseObject>("Request");

            user.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

            user.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            for(ParseObject user:objects){
                                user.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null) {
                                            Toast.makeText(riderActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                                        } else{
                                            Toast.makeText(riderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });

        }
    }

    public void checkForUpdates(){

        driverDetails=(TextView)findViewById(R.id.driverDetails);

        ParseQuery<ParseObject> query=ParseQuery.getQuery("Request");

        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        ParseQuery<ParseUser> driverD=ParseUser.getQuery();

                        driverD.whereEqualTo("username",objects.get(0).getString("driverUsername"));

                        driverD.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if(e==null){
                                    if(objects.size()>0){
                                        ParseGeoPoint driverLocation=objects.get(0).getParseGeoPoint("location");

                                        if(ContextCompat.checkSelfPermission(riderActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                                            Location userLastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                            if(userLastKnownLocation!=null){
                                                ParseGeoPoint userLocation=new ParseGeoPoint(userLastKnownLocation.getLatitude(),userLastKnownLocation.getLongitude());
                                                double distanceInMiles=userLocation.distanceInKilometersTo(driverLocation);
                                                double distanceInMilesRounded=Math.round(distanceInMiles*10);
                                                distanceInMilesRounded=distanceInMilesRounded/10;

                                                driverDetails.setText("Your driver is "+distanceInMilesRounded+" Km");

                                                LatLng driverLatLng=new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
                                                LatLng requestLatLng=new LatLng(userLastKnownLocation.getLatitude(),userLastKnownLocation.getLongitude());

                                                ArrayList<Marker> markers=new ArrayList<Marker>();


                                                LatLngBounds.Builder builder=new LatLngBounds.Builder();

                                                markers.add(mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Your Location")));
                                                markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Rider Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));


                                                for(Marker marker:markers){
                                                    builder.include(marker.getPosition());
                                                }

                                                LatLngBounds bounds=builder.build();

                                                int padding=25;

                                                CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,padding);

                                                mMap.moveCamera(cu);

                                                mMap.animateCamera(cu);


                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }else{
                        Log.i("no","user");
                    }
                }else{
                    Log.i("erooro",e.getMessage());
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                    }
                },2000);
            }
        });
    }
}
