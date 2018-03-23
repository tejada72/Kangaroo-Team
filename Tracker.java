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
import java.util.Timer;
import java.util.TimerTask;

public class Tracker extends AppCompatActivity implements LocationListener
{
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    LocationManager locationManager;
    LocationListener locationListener;
    double latitude;
    double longitude;

    Timer t = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        button = (Button) findViewById(R.id.locationSwitchbtn);
        text = (TextView) findViewById(R.id.locationSwitchlbl);
        xLabel = (TextView) findViewById(R.id.locationXtxt);
        yLabel = (TextView) findViewById(R.id.locationYtxt);


        if (buttonStatus == 1)
        {
            //Check permissions
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);


            }
        }
        else
        {
            button.setText("Off");
            button.setBackgroundColor(Color.RED);
            text.setText("Idle");
        }
        getLocation();

        //Turns location transmission off or on.
        button.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view)
            {
                switch (buttonStatus) {

                    //Turn on, if off
                    case 0:
                        text.setText("Tracking");
                        button.setText("On");
                        button.setBackgroundColor(Color.GREEN);
                        buttonStatus = 1;
                        //getLocation();
                        //xLabel.setText("" + longitude);
                        //yLabel.setText("" + latitude);
                        break;
                    //Turn off, if on
                    case 1:
                        text.setText("Idle");
                        button.setText("Off");
                        xLabel.setText("");
                        yLabel.setText("");
                        button.setBackgroundColor(Color.RED);
                        buttonStatus = 0;
                        break;
                    default:
                        break;
                }
            }
        });

        t.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                getLocation();
                xLabel.setText("" + longitude);
                yLabel.setText("" + latitude);
            }
        }, 0 , 10000);
    }


    @Override
    public void onLocationChanged(Location location) {
        xLabel.setText("Longitude:\n" + location.getLongitude());
        yLabel.setText("Latitude:\n" + location.getLatitude());
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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


    void getLocation()
    {
        try {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 5, this);
            //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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