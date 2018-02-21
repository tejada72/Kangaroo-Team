package teamkangaroo.areamonitoringtool;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Tracker extends AppCompatActivity
{
    Button button;
    TextView text;
    int buttonStatus = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        button = (Button) findViewById(R.id.locationSwitchbtn);
        text = (TextView) findViewById(R.id.locationSwitchlbl);

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
}
