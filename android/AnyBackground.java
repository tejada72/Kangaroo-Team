package teamkangaroo.areamonitoringtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by enz12 on 3/29/2018.
 */

public class AnyBackground extends AsyncTask<String, String, String>
{
    private Context ctx;
    Tracker tracker;
	String runId;
	String userId;
	String lon;
	String lat;
	String logTime;
	String status;
	String urlParams;

	//constructor for status change
	public AnyBackground(Context ctx, String runId, String userId, String status) {
		this.ctx = ctx;
		this.runId = runId;
		this.userId = userId;
		this.status = status;
		urlParams = "action=update-status&user-id=" + userId + "&run-id=" + runId
                    + "&status=" + status;
		}
	
	//contructor for location change
    public AnyBackground(Context ctx, Tracker tracker, String runId, String userId, 
	String lon, String lat, String logtime)
    {
        this.ctx = ctx;
        this.tracker = tracker;
		this.runId = runId;
		this.userId = userId;
		this.lon = lon;
		this.lat = lat;
		this.logtime = logtime;
		urlParams = "action=update-location&run-id=" + runId + "&user-id=" + userId
                    + "&lon=" + lon + "&lat=" + lat + "&log-time=" + logTime;
		
    }
	
	//constructor for main activity
	public AnyBackground(Context ctx, Class nextClass, String runId, String userId) {
		this.ctx = ctx;
		this.nextClass = nextClass;
		this.runId = runId;
		this.userId = userId;
		urlParams = "action=log-in&run-code=" + runCode + "&username=" + username;
		}

    @Override
    protected String doInBackground(String... params)
    {
		//delete later
        //runId = params[0];
        //userId = params[1];
        //lon = params[2];
        //lat = params[3];
        //logTime = params[4];

        try
        {
            URL url = new URL("http://ec2-54-157-62-1.compute-1.amazonaws.com/api/mobile.php");
            // delete later
			//String urlParams = "action=update-location&run-id=" + runId + "&user-id=" + userId
            //        + "&lon=" + lon + "&lat=" + lat + "&log-time=" + logTime;

            //System.out.print(urlParams);

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
        catch (ConnectException e)
        {
            e.printStackTrace();

            //Allows Toast to be made in Tracker
            publishProgress();
            return "Exception: "+e.getMessage();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "Exception: "+e.getMessage();
        }
    }

    @Override
    //This is what publishProgress() calls.
    protected void onProgressUpdate(String... values)
    {
        Toast.makeText(ctx, "Lost connection to server.\nAttempting to reconnect...",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String response)
    {
		//onPostExecute for the main activity
		onPostMainActivity();
	}
	else 
	{
		//onPostExecute for status change or location change
		onPostOthers();
	}
	
	private void onPostMainActivity() {
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
                    
                    JSONObject user_data = myResponse.getJSONObject("data");

                    run = user_data.getString("run-id");
                    username = user;
                    user = user_data.getString("user-id");

                    File sessionFile = new File(ctx.getFilesDir(), "session");
                    FileOutputStream outputStream;

                    try {
                        outputStream = new FileOutputStream(sessionFile);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        ArrayList<String> varData = new ArrayList<String>();
                        varData.add(run);
                        varData.add(user);
                        varData.add(userName.getText().toString());
                        objectOutputStream.writeObject(varData);
                        outputStream.close();
                    } catch (Exception e) {
                        System.err.println("Error occurs when saving state");
                        e.printStackTrace();
                    }

                    runID.setText("");
                    userName.setText("");

                   launchTracker();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                err = "Exception: "+e.getMessage();
            }
		}
		
		private void onPostOthers() {
		String err = null;

			try
			{
				JSONObject myResponse = new JSONObject(response.toString());

				//Check if run exists, or if name is taken
				if (myResponse.getBoolean("error"))
				{
					Toast.makeText(ctx, myResponse.getString("error-msg"), Toast.LENGTH_SHORT).show();
				}
				else
				{
					JSONObject user_data = myResponse.getJSONObject("data");
					boolean isActive = user_data.getBoolean("is-active");
              
					if (!isActive)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
						// Add the buttons
						builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								File sessionFile = new File(ctx.getFilesDir(), "session");
								sessionFile.delete();
								tracker.finish();
							}
						});
						builder.setCancelable(false);
						builder.setMessage("You will return to log in screen")
								.setTitle("RUN NO LONGER ACTIVE");
						builder.show();
						AlertDialog dialog = builder.create();
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
