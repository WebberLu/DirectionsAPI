package com.kymco.directionsapi;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String TAG = "DirectionsAPI";
    private GoogleMap mMap;
    private Marker mMyPosMarker = null;
    private static final float DEFAULT_ZOOM_SCALE = 15;
    private static final float DEFAULT_TILT_DEGREE = 30;

    private static int DIR_MODE = 0;
    private static int DIR_MODE_CAR = 0;
    private static int DIR_MODE_BICYCLE = 1;
    private static int DIR_MODE_WALKING = 2;
    private ImageView mCar, mBicycle, mWalking;
    private CheckBox mAvoidHighwaysCkb, mAvoidTollsCkb, mAvoidFerriesCkb;
    private Button mGO;
    private EditText mOriginEt, mDestinationEt;
    private ListView mList;
    private StepListAdapter mAdapter;
    boolean doubleBackToExitPressedOnce = false;
    private Polyline mNavRoute = null, mStepRoute = null;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final double EARTH_RADIUS = 6378137.0;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCar = (ImageView) findViewById(R.id.car_iv);
        mBicycle = (ImageView) findViewById(R.id.bicycle_iv);
        mWalking = (ImageView) findViewById(R.id.walking_iv);
        mAvoidHighwaysCkb = (CheckBox) findViewById(R.id.avoid_highways_ckb);
        mAvoidTollsCkb = (CheckBox) findViewById(R.id.avoid_tolls_ckb);
        mAvoidFerriesCkb = (CheckBox) findViewById(R.id.avoid_ferries_ckb);
        mGO = (Button) findViewById(R.id.go_bt);
        mOriginEt = (EditText) findViewById(R.id.origin_et);
        mOriginEt.setText("高雄");
        mDestinationEt = (EditText) findViewById(R.id.destination_et);
        mDestinationEt.setText("台南火車站");
        mGO.setOnClickListener(mClick);
        mCar.setOnClickListener(mClick);
        mBicycle.setOnClickListener(mClick);
        mWalking.setOnClickListener(mClick);
        mBicycle.setVisibility(View.GONE);
        selectDirMode(DIR_MODE_CAR);
        mList = (ListView) findViewById(R.id.listView);
        mAdapter = new StepListAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mOnItemClickListener);
        buildGoogleApiClient();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//      mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (mStepRoute != null)
                mStepRoute.remove();
            String polyline = "";
            polyline = mAdapter.getSteps().get(i).get("points");
            List<LatLng> list = new DirectionsJSONParser().decodePoly(polyline);
            PolylineOptions polylineOpt = new PolylineOptions();
            for (int j = 0; j < list.size(); j++) {
                polylineOpt.add(list.get(j));
            }
            polylineOpt.width(14).color(Color.RED);
            mStepRoute = mMap.addPolyline(polylineOpt);
            zoomMapInitial((ArrayList<LatLng>) list);
        }
    };

    @Override
    public void onBackPressed() {
        if (mAdapter.getLegs().size() > 0) {//reset direction list
            mAdapter.setLegs(new HashMap<String, String>());
            mAdapter.setSteps(new ArrayList<HashMap<String, String>>());
            mAdapter.notifyDataSetChanged();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private View.OnClickListener mClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == mCar) {
                selectDirMode(DIR_MODE_CAR);

            } else if (v == mBicycle) {
                selectDirMode(DIR_MODE_BICYCLE);

            } else if (v == mWalking) {
                selectDirMode(DIR_MODE_WALKING);

            } else if (v == mGO) {
                String url = "";
                url = Constants.API_URL +
                        "origin=" + mOriginEt.getText().toString() +
                        "&destination=" + mDestinationEt.getText().toString() +
                        "&language=" + Constants.LANGUAGE +
                        "&avoid=" + getAvoidParameter() +
                        "&mode=" + getModeParameter() +
                        "&sensor=false&units=metric" +
                        "&key=" + Constants.API_KEY;
                Log.i(Constants.TAG, url);
                new getJsonTask().execute(url);
            }

        }

    };

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        mGoogleApiClient.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Maps Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.kymco.directionsapi/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Maps Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.kymco.directionsapi/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
//        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Connected to GoogleApiClient");
// If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        startLocationUpdates();
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Double d = 0.0;
        Log.i(TAG, "onLocationChanged : lat : " + location.getLatitude() + " lng : " +
                "" + location.getLongitude() + " speed : " + location.getSpeed() + " Accuracy : " + location.getAccuracy());
        if (location.hasAccuracy() && location.getAccuracy() < 30) {
            if (mCurrentLocation != null) {
                d = gps2m(mCurrentLocation, location);
                Log.i(TAG, "onLocationChanged : pre location and latest location distance : " + d + " m");
            }
            mCurrentLocation = location;
            if (mMap != null && d!=0.0) {
                LatLng position = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                CameraPosition focusPos = new CameraPosition.Builder()
                        .target(position).zoom(DEFAULT_ZOOM_SCALE).bearing(0).tilt(DEFAULT_TILT_DEGREE).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(focusPos));
            }
        }
    }

    private Double gps2m(Location loc1, Location loc2) {
        double radLat1 = (loc1.getLatitude() * Math.PI / 180.0);
        double radLat2 = (loc2.getLatitude() * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (loc1.getLongitude() - loc2.getLongitude()) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private class getJsonTask extends AsyncTask<String, String, String> {
        ProgressDialog dialog;

        @Override
        protected void onPostExecute(String result) {

            Log.i(Constants.TAG, result);
            super.onPostExecute(result);

            DirectionsJSONParser parser = new DirectionsJSONParser();

            try {
                parser.setmJSONData(new JSONObject(result));
                List<HashMap<String, String>> legs = parser.getLegs();
                List<List<HashMap<String, String>>> steps = parser.getSteps();
                if (legs.size() > 0)
                    mAdapter.setLegs(legs.get(0));//第一條路線
                if (steps.size() > 0)
                    mAdapter.setSteps(steps.get(0));//第一條路線
                mAdapter.notifyDataSetChanged();

                ArrayList<LatLng> list = new ArrayList<LatLng>();
                list.add(new LatLng(
                        Double.parseDouble(legs.get(0).get("start_location_lat")),
                        Double.parseDouble(legs.get(0).get("start_location_lng"))));
                list.add(new LatLng(
                        Double.parseDouble(legs.get(0).get("end_location_lat")),
                        Double.parseDouble(legs.get(0).get("end_location_lng"))));
                zoomMapInitial(list);

                List<List<HashMap<String, String>>> positions = parser.parserPolylinePoints();
                if (positions.size() > 0) {
                    List<HashMap<String, String>> step = positions.get(0);//選第一條路線
                    for (int j = 0; j < step.size(); j++) {
                        HashMap<String, String> points = step.get(j);
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(
                                        Double.parseDouble(points.get("lat")),
                                        Double.parseDouble(points.get("lng"))))
                                .anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R
                                        .drawable.circumference)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            dialog = Utils.presentLoadingDialog(MapsActivity.this);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return downloadUrl(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    ;

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(strUrl);
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()), 8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null) {
                    stringBuilder.append(strLine);
                }
                input.close();
            }
            data = stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (iStream != null)
                iStream.close();
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return data;
    }

    private void selectDirMode(int mode) {
        DIR_MODE = mode;
        mCar.setImageLevel(0);
        mBicycle.setImageLevel(0);
        mWalking.setImageLevel(0);
        if (mode == DIR_MODE_CAR) {
            mCar.setImageLevel(1);
        } else if (mode == DIR_MODE_BICYCLE) {
            mBicycle.setImageLevel(1);
        } else if (mode == DIR_MODE_WALKING) {
            mWalking.setImageLevel(1);
        }
    }

    private String getAvoidParameter() {
        String result = "";
        result = (mAvoidHighwaysCkb.isChecked() ? Constants.AVOID_HIGHWAYS : "") + "|"
                + (mAvoidTollsCkb.isChecked() ? Constants.AVOID_TOLLS : "") + "|"
                + (mAvoidFerriesCkb.isChecked() ? Constants.AVOID_FERRIES : "");
        return result;
    }

    private String getModeParameter() {
        String result = "";
        if (DIR_MODE == DIR_MODE_CAR) {
            result = Constants.DIR_MODE_CAR;
        } else if (DIR_MODE == DIR_MODE_BICYCLE) {
            result = Constants.DIR_MODE_BICYCLE;
        } else if (DIR_MODE == DIR_MODE_WALKING) {
            result = Constants.DIR_MODE_WALKING;
        }
        return result;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == 1) {
            Toast.makeText(MapsActivity.this, "click1", Toast.LENGTH_SHORT).show();
        } else {

        }
        return super.onMenuItemSelected(featureId, item);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    private void zoomMapInitial(ArrayList<LatLng> placelist) {
        try {
            LatLngBounds.Builder bc = new LatLngBounds.Builder();
            for (LatLng place : placelist)
                bc.include(place);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 140));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
