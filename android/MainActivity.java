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
import java.net.URL;

public class MainActivity extends AppCompatActivity {
//Get the MAC Address for user login to the operator.

    EditText runID;
    EditText userName;
    String RUNID = null;
    String USERNAME = null;
    String run;
    String user;

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
                    .setTitle("Old session available");
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

            Intent intent = new Intent(ctx, Tracker.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //User logs in
    public void login (View view)
    {
        run = runID.getText().toString();
        user = userName.getText().toString();
        BackGround b = new BackGround();
        b.execute(run, user);

    }


    class BackGround extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            String runCode = params[0];        //runName is for runID, since runID is already taken
            String username = params[1];
            String data = "";
            int temp;

            try
            {
                URL url = new URL("http://ec2-54-157-62-1.compute-1.amazonaws.com/api/mobile.php");
                String urlParams = "action=log-in&run-code=" + runCode + "&username=" + username;

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(urlParams.getBytes());
                os.flush();
                os.close();

                InputStream is = httpURLConnection.getInputStream();

                Reader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0;)
                    sb.append((char)c);
                String response = sb.toString();
                System.out.println(response);

                is.close();
                httpURLConnection.disconnect();

                return response;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                return "Exception: "+e.getMessage();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return "Exception: "+e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response)
        {
            String err = null;

            try
            {
                JSONObject myResponse = new JSONObject(response.toString());

                //Check if run exists, or if name is taken
                if (myResponse.getBoolean("error"))
                {
                    Toast.makeText(ctx, myResponse.getString("error-msg"), Toast.LENGTH_SHORT).show();
                    System.out.println("Not working");
                }
                else
                {
                    JSONObject root = new JSONObject(response);
                    JSONObject user_data = root.getJSONObject("data");

                    RUNID = user_data.getString("run-id");
                    USERNAME = user_data.getString("user-id");

                    File sessionFile = new File(ctx.getFilesDir(), "session");
                    FileOutputStream outputStream;

                    try {
                        outputStream = new FileOutputStream(sessionFile);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        ArrayList<String> varData = new ArrayList<String>();
                        varData.add(run);
                        varData.add(user);
                        objectOutputStream.writeObject(varData);
                        outputStream.close();
                    } catch (Exception e) {
                        System.err.println("Error occurs when saving state");
                        e.printStackTrace();
                    }

                    Intent i = new Intent(ctx, Tracker.class);
                    i.putExtra("runId", RUNID);
                    i.putExtra("userId", USERNAME);
                    startActivity(i);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                err = "Exception: "+e.getMessage();
            }


        }
    }
}
