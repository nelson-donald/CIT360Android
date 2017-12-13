package com.example.neldo.atm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    Button submit;
    EditText code;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences(Prefs.MyPREFERENCES, Context.MODE_PRIVATE);

        //Run my custom method to grab the widgets for access to their properties
        findViewsById();
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new Login().execute();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private class Login extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Getting username and password from user input
            String pinCode = code.getText().toString();


            StringBuilder result = new StringBuilder();
            try {
                //Create the Account object
                Account account = new Account();

                //Default the account number for testing.
                account.setAccountNumber("12345");

                //Set the pincode from the text field
                account.setPinCode(pinCode);

                //Create the GSON object
                Gson g = new Gson();

                //Create the HttpURLConnection and send the request
                URL url = new URL(Prefs.URILogon);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("POST");
                try (DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream())) {
                    //wr.writeBytes(URLEncoder.encode("a=12345&p=" + pinCode,"UTF-8"));
                    wr.writeBytes(URLEncoder.encode(g.toJson(account),"UTF-8"));
                    wr.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }



                //read the inputstream and saves it
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;

                while ((line = reader.readLine()) != null) {
                    //Log.d("RESPONSE",line);
                    result.append(line);
                }
            }
            ((HttpURLConnection) connection).disconnect();

                System.out.println(result);




            } catch (Exception  e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return result.toString();
        }

        protected void onPostExecute(String result){

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Prefs.JSON, result);
            editor.apply();
            editor.commit();

            //Go to the next page
            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
        }

    }

    private void findViewsById(){
        submit = (Button)findViewById(R.id.btnLogin);
        code = (EditText)findViewById(R.id.editTextCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
