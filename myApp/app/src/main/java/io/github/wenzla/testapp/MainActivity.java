package io.github.wenzla.testapp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// also some of my layout element names are ass
public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "Main Activity";
    LocationManager locationManager;
    String provider;
    private Location gpsLocation;
    private CallbackManager callbackManager;
    private TextView info;
    public String s;
    public static String FBName;
    public static String GPSLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        Log.d(TAG, "OnCreate");
        callbackManager = CallbackManager.Factory.create();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        info = findViewById(R.id.locationString);
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String userID = loginResult.getAccessToken().getUserId();
                /* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/"+userID,
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null && response.getJSONObject() != null && response.getJSONObject().has("name"))
                                {
                                    try {
                                        s = (response.getJSONObject().getString("name"));
                                        setFBName(s);
                                        GetCoordinates();
                                    } catch (JSONException e) {
                                        Log.e(TAG, "onCompleted: ",e );
                                    }
                                }
                            }

                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                String s = "Login attempt cancelled.";
                setFBName(s);
            }

            @Override
            public void onError(FacebookException e) {
                String s = "Login attempt failed.";
                setFBName(s);
            }

        });

        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        gpsLocation = locationManager.getLastKnownLocation(provider);

        // This is recycled code but idk how to make a GraphRequest.Callback() its own method.
        if(isLoggedIn()){
            String UserID = AccessToken.getCurrentAccessToken().getUserId();
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/"+UserID,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            if (response != null && response.getJSONObject() != null && response.getJSONObject().has("name"))
                            {
                                try {
                                    s = (response.getJSONObject().getString("name"));
                                    setFBName(s);
                                    GetCoordinates();
                                } catch (JSONException e) {
                                    Log.e(TAG, "onCompleted: ",e );
                                }
                            }
                        }

                    }
            ).executeAsync();
        }

    }

    public void SetupClick(View v) {
        startActivity(new Intent(MainActivity.this, LocalMultiplayerGame.class));
    }

    public void sessionTest(View v) {
        if (DBHandler.ping()) {
            startActivity(new Intent(MainActivity.this, MultiplayerGame.class));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setMessage("Unable to connect");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setContentView(R.layout.home_screen);
        Log.d(TAG, "OnStart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.home_screen);
        Log.d(TAG, "OnResume");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, (LocationListener) this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }


    @Override
    public void onLocationChanged(Location location) {
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Location info: Lat", lat.toString());
        Log.i("Location info: Lng", lng.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.e(TAG, "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    // This is code can be used for all permissions
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // If user denies permissions, sends them back to the main activity
                    Intent i=new Intent(
                            MainActivity.this,
                            MainActivity.class);
                    startActivity(i);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void GetCoordinates(){
        // used the geocoder to find location based on latitude and longitude
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        Double latitude = gpsLocation.getLatitude();
        Double longitude = gpsLocation.getLongitude();

        // If the user allowed permissions, gets the location
        if(checkLocationPermission()) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                //String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String country = addresses.get(0).getCountryName();
                String state = addresses.get(0).getAdminArea();
                String LocationString = state +", " + country;
                setLocation(LocationString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Otherwise, asks for permissions again.
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void setFBName(String name) {
        //SessionHandler.setData(name,AccessToken.getCurrentAccessToken().getUserId());
        int count = 0; //SessionHandler.getWins();
        String wincount = "You have won "+count+" game";
        if (count!=1) {
            wincount = wincount+"s";
        }
        if (count<0) {
            wincount = "Log in to save your progress";
        }
        ((TextView)findViewById(R.id.r_u_a_scrub)).setText(wincount);
        info = findViewById(R.id.locationString);
        FBName = name;
        String newString = (String)info.getText();
        newString = newString + name;
        info.setText(newString);
    }

    public void setLocation(String location) {
        info = findViewById(R.id.locationString);
        GPSLocation = location;
        String newString = (String)info.getText();
        newString = newString + '\n' + "from " + location;
        info.setText(newString);
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}
