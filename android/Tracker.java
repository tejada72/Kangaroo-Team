package teamkangaroo.areamonitoringtool;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

public class Tracker extends AppCompatActivity implements LocationListener
{
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    FusedLocationProviderClient locationClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    double latitude = 0.0;   //lat
    double longitude = 0.0;  //long

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        button = (Button) findViewById(R.id.locationSwitchbtn);
        text = (TextView) findViewById(R.id.locationSwitchlbl);
        xLabel = (TextView) findViewById(R.id.locationXtxt);
        yLabel = (TextView) findViewById(R.id.locationYtxt);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(Tracker.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Need to prompt user with Android dialog that asks to use GPS
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        System.out.println("Location success!");
                        if (location == null) {
                            System.out.println("Location is null!");
                        }
                    }
                });

        //start asking for periodic location updates
        createLocationRequest();


        //anonymous class to do location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if(buttonStatus == 1) {
                        xLabel.setText(Double.toString(latitude));
                        yLabel.setText(Double.toString(longitude));
                    }
                }
            };
        };

        locationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

        //Turns location transmission off or on.
        button.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view)
            {
                switch (buttonStatus) {

                    //Turn off
                    case 0:
                        text.setText("Location is being transmitted");
                        button.setText("On");
                        button.setBackgroundColor(Color.GREEN);
                        //Supposedly printing location grabbed when tracker started in OnCreate listener
                        //need some way to write the update, perhaps a handler
                        buttonStatus = 1;
                        break;
                    //Turn on
                    case 1:
                        text.setText("Location is not being transmitted");
                        button.setText("Off");
                        button.setBackgroundColor(Color.RED);
                        xLabel.setText("0.0");
                        yLabel.setText("0.0");
                        buttonStatus = 0;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    //asks for periodic updates
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //creates location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
    }

}
