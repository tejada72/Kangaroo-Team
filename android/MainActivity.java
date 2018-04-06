package teamkangaroo.areamonitoringtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
//Get the MAC Address for user login to the operator.

    EditText runID;
    EditText userName;
    String run;
    String user;
    String username;

    Context ctx = this;

//"Run does not exist" function
//Check the run.
    //User puts in RunID.
    //Check ID with URL.
    //Continue if it exists.
    //Ask again if it does not exist.
    //private static HttpURLConnection con;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runID = (EditText) findViewById(R.id.runName);
        userName = (EditText) findViewById(R.id.userName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        File sessionFile = new File(ctx.getFilesDir(), "session");

        if(sessionFile.canRead()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    restoreSession();
                }
            });
            builder.setNeutralButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    File sessionFile = new File(ctx.getFilesDir(), "session");
                    sessionFile.delete();
                }
            });
            builder.setMessage("Restore previous session?")
                    .setTitle("You have a session available");
            builder.show();
            AlertDialog dialog = builder.create();
        }
    }

    protected void restoreSession() {
        File sessionFile = new File(ctx.getFilesDir(), "session");
        FileInputStream inFileStream;
        try {
            inFileStream = new FileInputStream(sessionFile);
            ObjectInputStream objInputStream = new ObjectInputStream(inFileStream);
            ArrayList<String> prevSessionData = (ArrayList) objInputStream.readObject();

            this.run = prevSessionData.get(0);
            this.user = prevSessionData.get(1);
            this.username = prevSessionData.get(2);

            launchTracker();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //User logs in
    public void login (View view)
    {
        run = runID.getText().toString();
        user = userName.getText().toString();
        final AnyBackground b = new AnyBackground(ctx,run,user);
        File sessionFile = new File(ctx.getFilesDir(), "session");
        if(sessionFile.canRead()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            // Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    File sessionFile = new File(ctx.getFilesDir(), "session");
                    sessionFile.delete();
                    b.execute();
                }
            });
            builder.setNeutralButton("Use old session", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    restoreSession();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.setMessage("Delete this session and proceed?")
                    .setTitle("Active session available");
            builder.show();
            AlertDialog dialog = builder.create();
        } else {
            b.execute();
        }

    }

        private void launchTracker() {
        Intent intent = new Intent(ctx, Tracker.class);
        intent.putExtra("runId",run);
        intent.putExtra("userId",user);
        intent.putExtra("username",username);
        startActivity(intent);
    }
}
