package teamkangaroo.areamonitoringtool;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class Tracker extends AppCompatActivity implements LocationListener
{
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    LocationManager locationManager;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        button = (Button) findViewById(R.id.locationSwitchbtn);
        text = (TextView) findViewById(R.id.locationSwitchlbl);
        xLabel = (TextView) findViewById(R.id.locationXtxt);
        yLabel = (TextView) findViewById(R.id.locationYtxt);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);


        }
        getLocation();
        /*
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        */

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
                        buttonStatus = 1;
                        getLocation();
                        break;
                    //Turn on
                    case 1:
                        text.setText("Location is not being transmitted");
                        button.setText("Off");
                        button.setBackgroundColor(Color.RED);
                        buttonStatus = 0;

                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        xLabel.setText("Latitude: " + location.getLatitude());
        yLabel.setText("Longitude: " + location.getLongitude());
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //lists entire address of user's location
            //yLabel.setText(yLabel.getText() + "\n"+addresses.get(0).getAddressLine(0)+", "+
                    //addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
        }catch(Exception e)
        {

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(Tracker.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            longitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
            latitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
            longitude = 0;
            latitude = 0;
        }
    }

}