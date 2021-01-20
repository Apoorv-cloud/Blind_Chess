package io.github.wenzla.testapp;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Steven on 10/24/2017.
 */

public class DBRequester extends AsyncTask<String, String, String> {

    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection urlConnection;
        OutputStream out;
        BufferedReader br;
        StringBuilder sb;

        String urlString = params[0];
        String data = params[1];

        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();
            urlConnection.connect();

            br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
            sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return "failed connection";
    }
}
