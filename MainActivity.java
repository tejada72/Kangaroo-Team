package teamkangaroo.areamonitoringtool;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

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
    }

    //User logs in
    public void login (View view)
    {
        run = runID.getText().toString();
        user = userName.getText().toString();
        BackGround b = new BackGround();
        b.execute(run, user);
        //Intent intent = new Intent(this, Tracker.class);
        //startActivity(intent);
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

                try {
                    JSONObject myResponse = new JSONObject(response.toString());
                    if(myResponse.getBoolean("error")) {
                        //Toast.makeText(ctx, myResponse.getString("error-msg"), Toast.LENGTH_SHORT).show();
                        System.out.println("Not working");
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                    return "Exception: " +e.getMessage();
                }

                while((temp=is.read()) != -1)
                {
                    data += (char)temp;
                }

                is.close();
                httpURLConnection.disconnect();

                return data;
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
        protected void onPostExecute(String s)
        {
            String err = null;
            try
            {
                JSONObject root = new JSONObject(s);
                JSONObject user_data = root.getJSONObject("user_data");

                RUNID = user_data.getString("run-code");
                USERNAME = user_data.getString("username");
                //PASSWORD = user_data.getString("password");
                //EMAIL = user_data.getString("email");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                err = "Exception: "+e.getMessage();
            }

            Intent i = new Intent(ctx, Tracker.class);
            //i.putExtra("run-code", RUNID);
            //i.putExtra("username", USERNAME);
            //i.putExtra("password", PASSWORD);
            //i.putExtra("email", EMAIL);
            //i.putExtra("err", err);
            startActivity(i);
        }
    }
}