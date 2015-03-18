package chrisdalzell.livescoregames;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends FragmentActivity {

    Button buttonCheckPassword;
    EditText editTextPassword;
    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonCheckPassword = (Button) findViewById(R.id.buttonCheckPassword);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonCheckPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get current internet connection status
                final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (!(activeNetwork != null && activeNetwork.isConnected())) {
                    // If user not connected to internet then redirect them to wifi settings
                    displayToast("Please connect to either wifi or a mobile network then click button again");
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    // If user is connected to internet then check if the password is correct
                    new CheckPassword().execute(GameSelectionActivity.SERVER_ADDRESS + "check_password.php");
                }
            }
        });
    }

    // Displays a toast with passed in message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // AsyncTask which checks if game exists using php script on server
    private class CheckPassword extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            HttpClient httpclient = new DefaultHttpClient();
            // Create HttpPost with script server address passed to asynctask
            HttpPost httppost = new HttpPost((String) objects[0]);
            try {
                // Add gameID to List<NameValuePair> and add to HttpPost
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("password", editTextPassword.getText().toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HttpPost and store response
                HttpResponse response = httpclient.execute(httppost);

                // Convert response into String
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                String line = reader.readLine();
                is.close();
                //System.out.println(line);

                // Trim unnecessary characters from response String
                message = line.trim();
            } catch (Exception e) {
                //System.out.println("CreateGameActivity: " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (message.equals("incorrect")) {
                // Display a toast if password is incorrect
                displayToast("The password entered is incorrect. Please try again.");
                editTextPassword.setText("");
                message = "";
            } else {
                // If password is correct, start GameSelectionActivity
                message = "";
                startActivity(new Intent(LoginActivity.this, GameSelectionActivity.class));
            }
        }
    }
}
