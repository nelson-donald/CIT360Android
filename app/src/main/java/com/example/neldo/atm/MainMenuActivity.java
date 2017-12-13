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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainMenuActivity extends AppCompatActivity {


    SharedPreferences sharedpreferences;
    Account account = new Account();
    TextView textViewBalance;
    Button buttonWithdrawal;
    Button buttonDeposit;
    EditText editTextWithdrawal;
    EditText editTextDeposit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
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

        findViewsById();

        buttonDeposit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new Deposit().execute();
            }
        });

        buttonWithdrawal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new Withdrawal().execute();
            }
        });


        sharedpreferences = getSharedPreferences(Prefs.MyPREFERENCES, Context.MODE_PRIVATE);

        //Thread t = new Thread();
        //t.start();

        //new Balance().execute(null,null,null);
        //Setup the Balance
        updateBalance();
    }

    public void updateBalance(){
        //read the stored json string
        String json = sharedpreferences.getString(Prefs.JSON,"");

        //Create the GSON object
        Gson g = new Gson();

        //Convert JSON back to class object
        account = g.fromJson(json,Account.class);

        //Update the text fields accordingly
        int i = account.getBalance();

        try{
            textViewBalance.setText("$" + String.valueOf(i));
            editTextWithdrawal.setText("");
            editTextDeposit.setText("");

        }catch (Exception ex)
        {
        }
    }


    private void findViewsById(){
        buttonWithdrawal = (Button)findViewById(R.id.buttonWithdrawal);
        buttonDeposit = (Button)findViewById(R.id.buttonDeposit);
        textViewBalance = (TextView)findViewById(R.id.textViewBalance);
        editTextWithdrawal = (EditText)findViewById(R.id.editTextWithdrawal);
        editTextDeposit = (EditText)findViewById(R.id.editTextDeposit);
    }




    private class Withdrawal extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // Getting username and password from user input
            String value = editTextWithdrawal.getText().toString();

            //read the stored json string
            String json = sharedpreferences.getString(Prefs.JSON,"");

            //Create the GSON object
            Gson g = new Gson();

            //Convert JSON back to class object
            Account accountWithdrawal = g.fromJson(json,Account.class);
            accountWithdrawal.setBalance(accountWithdrawal.getBalance() - Integer.parseInt(value));

            StringBuilder result = new StringBuilder();
            try {

                //Create the HttpURLConnection and send the request
                URL url = new URL(Prefs.URIWithdrawal);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("POST");
                try (DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream())) {
                    //wr.writeBytes(URLEncoder.encode("a=12345&p=" + pinCode,"UTF-8"));
                    wr.writeBytes(URLEncoder.encode(g.toJson(accountWithdrawal),"UTF-8"));
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

            //reapply the balance
            updateBalance();
        }

    }

    private class Deposit extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {


            StringBuilder result = new StringBuilder();
            try {

                // Getting username and password from user input
                String value = editTextDeposit.getText().toString();

                //read the stored json string
                String json = sharedpreferences.getString(Prefs.JSON,"");

                //Create the GSON object
                Gson g = new Gson();

                //Convert JSON back to class object
                Account accountDeposit = g.fromJson(json,Account.class);
                accountDeposit.setBalance(accountDeposit.getBalance() + Integer.parseInt(value));

                //Create the HttpURLConnection and send the request
                URL url = new URL(Prefs.URIDeposit);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("POST");
                try (DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream())) {
                    //wr.writeBytes(URLEncoder.encode("a=12345&p=" + pinCode,"UTF-8"));
                    wr.writeBytes(URLEncoder.encode(g.toJson(accountDeposit),"UTF-8"));
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

            //reapply the balance
            updateBalance();
        }

    }


}
