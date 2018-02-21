package teamkangaroo.areamonitoringtool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
//Get the MAC Address for user login to the operator.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //User logs in
    public void login (View view)
    {
        Intent intent = new Intent(this, Tracker.class);
        startActivity(intent);
    }
}
