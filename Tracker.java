package teamkangaroo.areamonitoringtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.location.Location;
import android.widget.Toast;

import java.io.File;
import java.sql.Timestamp;

public class Tracker extends AppCompatActivity {
    Button btnLocation;      //Button for on/off btnLocation
    Button btnEmergency;    //Button for Emergency state
    Button btnFlag;         //Button for Place Flag button
    TextView text;      //TextView for the Button Message
    TextView xLabel;    //TextView for X coordinates
    TextView yLabel;    //TextView for Y coordinates
    TextView lblEmergency; //TextView for emergency Status
    TextView lblIsLeader;

    int buttonStatus = 1;
    boolean emergency = false;
    boolean isLeader = false;
    String userId;
    String runId;
    String username;

    int counter = 0; //Counter for updating location data

    GPSHandler gps;

    double latitude = 0.0;   //lat
    double longitude = 0.0;  //long

    Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        btnLocation = (Button) findViewById(R.id.locationSwitchbtn);
        btnEmergency = (Button) findViewById(R.id.btnEmergency);
        btnFlag = (Button) findViewById(R.id.btnFlag);
        text = (TextView) findViewById(R.id.locationSwitchlbl);
        xLabel = (TextView) findViewById(R.id.locationX);
        yLabel = (TextView) findViewById(R.id.locationY);
        lblEmergency = (TextView) findViewById(R.id.lblEmergency);
        lblIsLeader = (TextView) findViewById(R.id.lblIsLeader);

        //Creates the new GPS handler
        gps = new GPSHandler(this);

        //Check if the GPS is turn on
        gps.checkGPS();

        //Creates the btnLocation listener
        setButtonsListener();

        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("runId") != null) {
            runId = bundle.getString("runId");
        }
        if (bundle.getString("userId") != null) {
            userId = bundle.getString("userId");
        }
        if(bundle.getString("username") != null) {
            username = bundle.getString("username");
        }
    }

    /**
     * Creates the listener for the btnLocation that tracks
     */
    private void setButtonsListener() {
        //Turns location transmission off or on.
        btnLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                gps.checkGPS();
                switch (buttonStatus) {

                    //Turn on
                    case 0:
                        text.setText("Location is Loading");
                        btnLocation.setText("On");
                        btnLocation.setBackgroundColor(Color.GREEN);
                        xLabel.setText(Double.toString(longitude));
                        yLabel.setText(Double.toString(latitude));
                        btnEmergency.setEnabled(true);
                        btnEmergency.setBackgroundColor(Color.RED);
                        //Supposedly printing location grabbed when tracker started in OnCreate listener
                        //need some way to write the update, perhaps a handler
                        buttonStatus = 1;
                        changeStatus(buttonStatus);
                        break;
                    //Turn off
                    case 1:
                        text.setText("Location is not being transmitted");
                        btnLocation.setText("Off");
                        btnLocation.setBackgroundColor(Color.rgb(255, 165, 0));
                        xLabel.setText("0.0");
                        yLabel.setText("0.0");
                        btnEmergency.setEnabled(false);
                        btnEmergency.setBackgroundColor(Color.GRAY);
                        lblEmergency.setText("");
                        buttonStatus = 0;
                        changeStatus(buttonStatus);
                        break;
                    default:
                        break;
                }
            }
        });

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!emergency) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    // Add the buttons
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setEmergency(!emergency); //Reverse the state of emergency
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setMessage("Are you sure?")
                            .setTitle("Change emergency state");
                    builder.show();
                    AlertDialog dialog = builder.create();
                } else
                    setEmergency(!emergency); //Reverse the state of emergency
            }
        });

        btnFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String lat = Double.toString(latitude);
                final String lon = Double.toString(longitude);
                int maxLength = 255;

                AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
                final EditText edittext = new EditText(ctx);
                edittext.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(maxLength)
                });
                alert.setMessage("Flagged Location:\nLatitude: " + lat +
                        "\nLongitude: " + lon + "\nCharacter limit: " + maxLength);
                alert.setTitle("Flag Message");

                alert.setView(edittext);

                alert.setPositiveButton("Set Flag", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String flagMessage = edittext.getText().toString();
                        setFlag(lat, lon, flagMessage);
                        Toast.makeText(ctx, "Flag has been placed!",
                                Toast.LENGTH_SHORT).show();
                        System.out.println("Flag has been placed!");
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing
                    }
                });

                alert.show();
            }
        });
    }

    /**
     * Changes the location whenever the GPSHandler class has a location update
     *
     * @param location
     */
    public void changeLocation(Location location) {

        this.setLatitude(location.getLatitude());
        this.setLongitude(location.getLongitude());

        if (buttonStatus == 1) {
            yLabel.setText(Double.toString(latitude));
            xLabel.setText(Double.toString(longitude));

            if (!text.getText().equals("Location is being transmitted"))
                text.setText("Location is being transmitted");
        }

        if (counter == 0) {
            LocationBackground b = new LocationBackground(this, RunOver.class,this);
            Long time = (new Timestamp(System.currentTimeMillis())).getTime() / 1000;
            b.execute(runId, userId, Double.toString(longitude), Double.toString(latitude),
                    Long.toString(time));
            counter = 5;
        }

        counter--;
    }

    /**
     * Changes status whenever the user presses the on/off button or presses the
     * emergency button.
     *
     * @param status
     */
    public void changeStatus(int status)
    {
        StatusBackground s = new StatusBackground(this, RunOver.class, this);
        s.execute(runId, userId, Integer.toString(status));
    }

    /**
     * Sets the flag so the operator can view the flag on his/her map.
     * @param lat
     * @param lon
     * @param flagMessage
     */
    public void setFlag(String lat, String lon, String flagMessage)
    {
        FlagBackground f = new FlagBackground(this, RunOver.class, this);
        Long time = (new Timestamp(System.currentTimeMillis())).getTime() / 1000;
        f.execute(runId, userId, lon, lat, Long.toString(time), flagMessage);
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;

        if(emergency) {
            lblEmergency.setText("EMERGENCY STATE ACTIVATED");
            lblEmergency.setTextColor(Color.RED);

            //Change user status in database
            changeStatus(2);
        }
        else
        {
            //Checks the button status to set it back to the appropriate status
            if(buttonStatus == 1)
                changeStatus(buttonStatus);
            else
                changeStatus(0);
            lblEmergency.setText("");
        }
    }

    public void setLeader(boolean isLeader)
    {
        this.isLeader = isLeader;

        if(isLeader)
        {
            lblIsLeader.setText("Team Leader");
        }
        else
            lblIsLeader.setText("Team Member");
    }

    /**
     * Sets the longitude whenever a new location is found by the GPSHandler class.
     *
     * @param longitude - Double longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the latitude whenever a new location is found by the GPSHandler class.
     *
     * @param latitude - Double latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MenuItem item1 = item;

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                // Add the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File sessionFile = new File(ctx.getFilesDir(), "session");
                        sessionFile.delete();
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setMessage("Are you sure?")
                        .setTitle("Log out from " + username);
                builder.show();
                AlertDialog dialog = builder.create();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File sessionFile = new File(ctx.getFilesDir(), "session");
                sessionFile.delete();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setMessage("Are you sure?")
                .setTitle("Log out from " + username);
        builder.show();
        AlertDialog dialog = builder.create();
    }

}