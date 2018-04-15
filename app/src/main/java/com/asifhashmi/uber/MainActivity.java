package com.asifhashmi.uber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    boolean isRiderActive=true;
    Switch switchB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchB=(Switch)findViewById(R.id.switchB);


        if(ParseUser.getCurrentUser()!=null ) {
            if (!(ParseUser.getCurrentUser().getString("riderOrDriver")==null)) {
                redirect();
            }
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void getStarted(View view){
        Button getStartedB=(Button)findViewById(R.id.getStarted);
        String userType="rider";
        if(switchB.isChecked()){
            Log.i("Case","Driver");
            userType="driver";
        }else{
            Log.i("Case","Rider");
            userType="rider";
        }


        ParseUser.getCurrentUser().put("riderOrDriver",userType);

            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null){
                        Log.i("user","logged");
                        redirect();
                    }else{
                        Log.i("UserError",e.toString());
                    }
                }
            });

        Log.i("String",ParseUser.getCurrentUser().toString());


    }

    public void redirect(){
        Intent intent;
        if(switchB.isChecked()){
            intent=new Intent(getApplicationContext(),DriverActivityList.class);
            Log.i("Moving to driver","True");
        }else{
            intent=new Intent(getApplicationContext(),riderActivity.class);
            Log.i("Moving to rider","true");
        }

        startActivity(intent);
    }


}
