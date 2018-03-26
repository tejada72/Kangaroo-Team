package teamkangaroo.areamonitoringtool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.GoogleApiClient;

public class Tracker extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    FusedLocationProviderClient locationClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    ResolvableApiException resolve;

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
        //resolve = new ResolvableApiException();

    if (ContextCompat.checkSelfPermission(Tracker.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        // Permission is not granted
        // Need to prompt user with Android dialog that asks to use GPS
        //Takes user to GPS settings
        ActivityCompat.requestPermissions(Tracker.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(Tracker.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    Tracker.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

    /*if(((LocationManager)(getApplicationContext().getSystemService(Context.LOCATION_SERVICE))).isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
        try {
            resolve.startResolutionForResult(Tracker.this, 1);
        } catch (IntentSender.SendIntentException e) {
            System.out.println("Why won't you enable GPS?");
        }
    }*/

        locationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            System.out.println("Location success!");
                        }
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
                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        if (buttonStatus == 1) {
                            xLabel.setText(Double.toString(latitude));
                            yLabel.setText(Double.toString(longitude));
                        }
                    }
                }
            }

            ;
        };

        locationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

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
    @SuppressLint("RestrictedApi")
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}