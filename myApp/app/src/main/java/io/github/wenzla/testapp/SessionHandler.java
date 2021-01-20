package io.github.wenzla.testapp;

import android.util.Log;

import 	org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Steven on 11/1/2017.
 */

public class SessionHandler {

    private static String name = "Null Name";
    private static String id;
    private static boolean idSet = false;

    /*
     * status of connection
     * 0: do nothing
     * 1: waiting for connection
     * 2: waiting for turn
     */
    private static int status = 1;

    static Random r = new Random();

    private static String TAG = "SessionHandler";

    public static void setData(String n, String i) {
        name=n;
        if (i!=null) {
            id = i;
            idSet = true;
        }
    }

    public static int getWins() {
        if (idSet) {
            String response = DBHandler.send("SELECT * FROM Games WHERE WinID = '"+id+"'");
            try {
                JSONArray r = new JSONArray(response);
                return r.length();
            } catch (JSONException e) {
                Log.e(TAG, "-",e );
            }
        }
        return -1;
    }

    public static void createSession() {
        Log.d(TAG,"createSession");
        if (!idSet) {
            id = ""+ r.nextInt(10000);
        }
        String response = DBHandler.send("SELECT * FROM WaitingSession");
        Log.d(TAG,"createSession: "+response);
        JSONArray r;
        try {
            r = new JSONArray(response);

            if (r.length()>0) {
                JSONObject obj = r.getJSONObject(0);
                String otherID = obj.getString("uid");
                if (!otherID.equals(id)) {
                    String delete = "DELETE FROM WaitingSession WHERE uid = '" + otherID + "'";
                    Log.d(TAG, delete);
                    DBHandler.send(delete);
                    String insert = "INSERT INTO Session(p1uid, p2uid,turn) VALUES ('" + id + "','" + otherID + "','" + id + "')";
                    Log.d(TAG, insert);
                    DBHandler.send(insert);
                    Log.d(TAG, "session created");
                } else {
                    DBHandler.send("INSERT INTO WaitingSession(name,uid) VALUES ('"+name+"','"+id+"')");
                    Log.d(TAG,"wait created");
                }
            } else {
                DBHandler.send("INSERT INTO WaitingSession(name,uid) VALUES ('"+name+"','"+id+"')");
                Log.d(TAG,"wait created");
            }
        } catch (JSONException e) {
            Log.e(TAG, "-",e );
        }
    }

    public static boolean sessionConnect() {
        Log.d(TAG,"sessionConnect");
        String response = DBHandler.send("SELECT * FROM Session WHERE p1uid = '"+id+"' OR p2uid = '"+id+"'");
        Log.d(TAG,"sessionConnect: "+response);
        try {
            JSONArray r = new JSONArray(response);
            if (r.length()>0) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "-",e );
        }
        return false;
    }

    public static boolean isMyTurn() {
        Log.d(TAG,"isMyTurn");
        String response = DBHandler.send("SELECT * FROM Session WHERE turn = '"+id+"'");
        Log.d(TAG,"isMyTurn: "+response);
        try {
            JSONArray r = new JSONArray(response);
            if (r.length()>0) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "-",e );
        }
        return true;
    }

    public static void endTurn() {
        endTurn("",0,0,0,0);
    }

    public static void endTurn(String type, int from_rank, int from_file, int to_rank, int to_file) {
        SessionHandler.setStatus(0);
        String sendData = "piece = '"+type+"', from_rank = "+from_rank+", from_file = "+from_file+", to_rank = "+to_rank+", to_file = "+to_file+"";
        Log.d(TAG,"endTurn");
        if (!isMyTurn()) {
            return;
        }
        String response = DBHandler.send("SELECT * FROM Session WHERE turn = '"+id+"'");
        try {
            JSONArray r = new JSONArray(response);
            if (r.length()>0) {
                JSONObject obj = r.getJSONObject(0);
                String id1 = obj.getString("p1uid");
                String id2 = obj.getString("p2uid");
                if (id1.equals(id)) {
                    DBHandler.send("UPDATE Session SET turn = '"+id2+"', "+sendData+" WHERE turn = '"+id+"'");
                } else {
                    DBHandler.send("UPDATE Session SET turn = '"+id1+"' "+sendData+" WHERE turn = '"+id+"'");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "-",e );
        }
        SessionHandler.setStatus(2);
    }

    public static void recordWin() {
        if (idSet) {
            Log.d(TAG, "sessionConnect");
            String response = DBHandler.send("SELECT * FROM Session WHERE p1uid = '" + id + "' OR p2uid = '" + id + "'");
            Log.d(TAG, "sessionConnect: " + response);
            try {
                JSONArray r = new JSONArray(response);
                if (r.length() > 0) {
                    JSONObject obj = r.getJSONObject(0);
                    String id1 = obj.getString("p1uid");
                    String id2 = obj.getString("p2uid");
                    if (id1 == id) {
                        DBHandler.send("INSERT INTO Game(WinID,LoseID) VALUES('" + id1 + "','" + id2 + "')");
                    } else {
                        DBHandler.send("INSERT INTO Game(WinID,LoseID) VALUES('" + id2 + "','" + id1 + "')");
                    }

                } else {
                }
            } catch (JSONException e) {
                Log.e(TAG, "-", e);
            }
        }
    }

    public static Move getLastMove() {
        String response = DBHandler.send("SELECT * FROM Session WHERE p1uid = '"+id+"' OR p2uid = '"+id+"'");
        Move m = null;
        try {
            JSONArray r = new JSONArray(response);
            JSONObject obj = r.getJSONObject(0);
            String type = obj.getString("piece");
            int from_rank = obj.getInt("from_rank");
            int from_file = obj.getInt("from_file");
            int to_rank = obj.getInt("to_rank");
            int to_file = obj.getInt("to_file");

            Location from = new Location(from_rank,from_file);
            Location to = new Location(to_rank,to_file);
            Piece p = new Piece(type,from,0);
            m = new Move(p,from,to);
        } catch (JSONException e) {
            Log.e(TAG, "-",e );
        }
        return m;
    }

    public static void endSession() {
        Log.d(TAG,"endSession");
        setStatus(0);
        DBHandler.send("DELETE FROM WaitingSession WHERE uid = '"+id+"'");
        DBHandler.send("DELETE FROM Session WHERE p1uid = '"+id+"' OR p2uid = '"+id+"'");
    }

    public static int status() {
        return status;
    }

    public static void setStatus(int n) {
        status = n;
    }
}
