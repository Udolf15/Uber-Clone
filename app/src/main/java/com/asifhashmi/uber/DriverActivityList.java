package com.asifhashmi.uber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverActivityList extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    ListView requestList;
    ArrayAdapter arrayAdapter;
    List<String> reqList;
    List<Double> reqLatitude;
    List<Double> reqLongitude;
    List<String> username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        requestList=(ListView)findViewById(R.id.reqListView);

        reqLatitude=new ArrayList<Double>();
        reqLongitude=new ArrayList<Double>();
        username=new ArrayList<String>();

        username.clear();
        reqLongitude.clear();
        reqLatitude.clear();

        reqList=new ArrayList<String>();
        reqList.clear();

        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,reqList);

        requestList.setAdapter(arrayAdapter);

        requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              Intent intent=new Intent(getApplicationContext(),DriverMapActivity.class);

                if(ContextCompat.checkSelfPermission(DriverActivityList.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location userLastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(userLastKnownLocation!=null){
                        intent.putExtra("reqLat",reqLatitude.get(i));
                        intent.putExtra("reqLon",reqLongitude.get(i));
                        intent.putExtra("driverLat",userLastKnownLocation.getLatitude());
                        intent.putExtra("driverLon",userLastKnownLocation.getLongitude());
                        intent.putExtra("username",username.get(i));
                        startActivity(intent);
                    }
                }

            }

        });

        locationManager =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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
                updateListView(userLastKnownLocation);
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
                        updateListView(userLastKnownLocation);
                    }
                }
            }
        }
    }

    public void updateListView(Location location){
        Log.i("LOc",String.valueOf(location.getLatitude()));
        if(location!=null){
            ParseQuery<ParseObject> reqQuery=new ParseQuery<ParseObject>("Request");
           final ParseGeoPoint youLocation=new ParseGeoPoint(location.getLatitude(),location.getLongitude());

            reqQuery.whereNear("location",youLocation);

            reqQuery.setLimit(10);

            reqQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            reqList.clear();
                            for(ParseObject distance:objects){
                                username.add(distance.getString("username"));
                                reqLatitude.add(distance.getParseGeoPoint("location").getLatitude());
                                reqLongitude.add(distance.getParseGeoPoint("location").getLongitude());
                                double distanceInMiles=youLocation.distanceInKilometersTo(distance.getParseGeoPoint("location"));
                                double distanceInMilesRounded=Math.round(distanceInMiles*10);
                                distanceInMilesRounded=distanceInMilesRounded/10;
                                String distanceInKmString=distanceInMilesRounded+" Km";
                                reqList.add(distanceInKmString);
                            }
                            arrayAdapter.notifyDataSetChanged();
                        }else{
                            Log.i("Out","No One");
                        }
                    }else{
                        Log.i("asfda","error bro");
                    }
                }
            });

        }

    }
}
