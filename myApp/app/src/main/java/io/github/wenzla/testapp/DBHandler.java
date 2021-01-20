package io.github.wenzla.testapp;

import android.util.Log;

/**
 * Created by Steven on 10/24/2017.
 */

public class DBHandler {
    public static String send(String data) {
        DBRequester dbh = new DBRequester();
        dbh.execute("http://arceus.org/appclass.php",data);
        try {
            return dbh.get();
        } catch (Exception e) {
            return "failed execution";
        }
    }
    public static boolean ping() {
        String result = send("SELECT * FROM Session");
        Log.d("DBHandler",result);
        return !(result.equals("failed connection") || result.equals("failed execution"));
    }
}
