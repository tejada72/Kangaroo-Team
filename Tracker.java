package teamkangaroo.areamonitoringtool;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Tracker extends AppCompatActivity implements LocationListener
{
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        button = (Button) findViewById(R.id.locationSwitchbtn);
        text = (TextView) findViewById(R.id.locationSwitchlbl);
        xLabel = (TextView) findViewById(R.id.locationXtxt);
        yLabel = (TextView) findViewById(R.id.locationYtxt);

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
}
