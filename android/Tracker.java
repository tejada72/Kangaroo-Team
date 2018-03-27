package teamkangaroo.areamonitoringtool;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.location.Location;

public class Tracker extends AppCompatActivity  {
    Button button;      //Button for on/off button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    int buttonStatus = 1;

    GPSHandler gps;

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

        //Creates the new GPS handler
        gps = new GPSHandler(this);

        //Check if the GPS is turn on
        gps.checkGPS();

        //Creates the button listener
        setButtonListener();
    }

    /**
     * Creates the listener for the button that tracks
     */
    private void setButtonListener() {
        //Turns location transmission off or on.
        button.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view)
            {
                gps.checkGPS();
                switch (buttonStatus) {

                    //Turn on
                    case 0:
                        text.setText("Location is Loading");
                        button.setText("On");
                        button.setBackgroundColor(Color.GREEN);
                        xLabel.setText(Double.toString(latitude));
                        yLabel.setText(Double.toString(longitude));
                        //Supposedly printing location grabbed when tracker started in OnCreate listener
                        //need some way to write the update, perhaps a handler
                        buttonStatus = 1;
                        break;
                    //Turn off
                    case 1:
                        text.setText("Location is not being transmitted");
                        button.setText("Off");
                        button.setBackgroundColor(Color.RED);
                        xLabel.setText("Longitude");
                        yLabel.setText("Latitude");
                        buttonStatus = 0;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void changeLocation(Location location) {

        this.setLatitude(location.getLatitude());
        this.setLongitude(location.getLongitude());

        if(buttonStatus == 1) {
            yLabel.setText(Double.toString(longitude));
            xLabel.setText(Double.toString(latitude));

            if(!text.getText().equals("Location is being transmitted"))
                text.setText("Location is being transmitted");
        }
    }

    /**
     * Sets the longitude whenever a new location is found by the GPSHandler class.
     * @param longitude - Double longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the latitude whenever a new location is found by the GPSHandler class.
     * @param latitude - Double latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
