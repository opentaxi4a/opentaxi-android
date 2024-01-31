package com.opentaxi.opentaxipassenger.views;

import static com.opentaxi.opentaxipassenger.app.application.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentaxi.opentaxipassenger.R;
import com.opentaxi.opentaxipassenger.app.LocationTrack;
import com.opentaxi.opentaxipassenger.app.app;
import com.opentaxi.opentaxipassenger.app.application;
import com.opentaxi.opentaxipassenger.app.shareData;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.OneFingerZoomOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    RequestQueue requestQueue;
    // ===========================================================
    // Constants
    // ===========================================================

    private static final String PREFS_LATITUDE_STRING = "latitudeString";
    private static final String PREFS_LONGITUDE_STRING = "longitudeString";
    private static final String PREFS_ORIENTATION = "orientation";
    private static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    //this points select by user from map and send to server for calculate price and submit trip
    private GeoPoint firstPoint;
    private Marker firstMarker;
    private GeoPoint secondPoint;
    private Marker secondMarker;
    private GeoPoint thirdPoint;
    private Marker thirdMarker;
    MyLocationNewOverlay mLocationOverlay;

    ScaleBarOverlay mScaleBarOverlay;

    OneFingerZoomOverlay mOneFingerZoomOverlay;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set volley request object
        requestQueue = Volley.newRequestQueue(this);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        // set null all markers and points
        firstPoint = null;
        secondPoint = null;
        thirdPoint = null;


        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            map.setOnGenericMotionListener(new View.OnGenericMotionListener() {
                /**
                 * mouse wheel zooming ftw
                 * http://stackoverflow.com/questions/11024809/how-can-my-view-respond-to-a-mousewheel
                 * @param v
                 * @param event
                 * @return
                 */
                @Override
                public boolean onGenericMotion(View v, MotionEvent event) {
                    if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
                        if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                map.getController().zoomOut();
                            else {
                                //this part just centers the map on the current mouse location before the zoom action occurs
                                IGeoPoint iGeoPoint = map.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                                map.getController().animateTo(iGeoPoint);
                                map.getController().zoomIn();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        //add zoom with multi touch
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);

        //map controller
        IMapController mapController = map.getController();
        mapController.setZoom(16);

        //disable map replication in x and y
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        //check permissions
        String[] perms = {
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        requestPermissionsIfNecessary(perms);

        //My Location
        //note you have handle the permissions yourself, the overlay did not do it for you
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        //map scale bar
        final DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(this.mScaleBarOverlay);

        // support for one finger zoom
        mOneFingerZoomOverlay = new OneFingerZoomOverlay();
        map.getOverlays().add(this.mOneFingerZoomOverlay);

        //needed for pinch zooms
        map.setMultiTouchControls(true);

        //scales tiles to the current screen's DPI, helps with readability of labels
        map.setTilesScaledToDpi(true);

        //the rest of this is restoring the last map location the user looked at
        final float zoomLevel = shareData.getShareData().getFloat(PREFS_ZOOM_LEVEL_DOUBLE, 1);
        map.getController().setZoom(zoomLevel);
        final String latitudeString = shareData.getShareData().getString(PREFS_LATITUDE_STRING, "1.0");
        final String longitudeString = shareData.getShareData().getString(PREFS_LONGITUDE_STRING, "1.0");
        final double latitude = Double.parseDouble(latitudeString);
        final double longitude = Double.parseDouble(longitudeString);
        map.setExpectedCenter(new GeoPoint(latitude, longitude));
    }

    @Override
    public void onPause() {
        //save the current location
        final SharedPreferences.Editor edit = shareData.getShareData().edit();
        edit.putFloat(PREFS_ORIENTATION, map.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(map.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(map.getMapCenter().getLongitude()));
        edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, (float) map.getZoomLevelDouble());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            edit.apply();
        }
        map.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if(secondPoint != null){
            //window came back from previous activity
            removeSecondMarker();
        }
        super.onResume();
        map.onResume();
    }

    public void removeSecondMarker(){
        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker&&((Marker) overlay).getId().equals("secondMarker")){
                map.getOverlays().remove(overlay);
                map.setExpectedCenter(secondPoint);
                secondPoint = null;
            }
        }
    }

    public void removeFirstMarker(){
        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker&&((Marker) overlay).getId().equals("firstMarker")){
                map.getOverlays().remove(overlay);
                map.setExpectedCenter(firstPoint);
                firstPoint = null;
            }
        }
        //ui operations
        Button btnSource = findViewById(R.id.btnSelectSource);
        btnSource.setText(R.string.set_source);
    }
    @Override
    public void onBackPressed() {
        if(firstPoint == null){
            super.onBackPressed();
        }
        else if(secondPoint != null){
            removeSecondMarker();
        }
        else{
            removeFirstMarker();
        }
        //super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public static class MyLocationOverlayWithClick extends MyLocationNewOverlay {

        public MyLocationOverlayWithClick(MapView mapView) {
            super(mapView);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, MapView map) {
            if (getLastFix() != null)
                Toast.makeText(map.getContext(), "Tap! I am at " + getLastFix().getLatitude() + "," + getLastFix().getLongitude(), Toast.LENGTH_LONG).show();
            return true;

        }
    }

    //defined functions
    public void btnSetLocation(View view){
        final MyLocationOverlayWithClick overlay = new MyLocationOverlayWithClick(map);
        IMapController mapController = map.getController();
        locationTrack = new LocationTrack(MainActivity.this);


        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            GeoPoint startPoint = new GeoPoint(latitude, longitude);
            mapController.setCenter(startPoint);
            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

        } else {

            locationTrack.showSettingsAlert();
        }
    }

    public void zoomIn() {
        map.getController().zoomIn();
    }

    public void zoomOut() {
        map.getController().zoomOut();
    }

    // @Override
    // public boolean onTrackballEvent(final MotionEvent event) {
    // return this.mMapView.onTrackballEvent(event);
    // }
    public void invalidateMapView() {
        map.invalidate();
    }


    //this function mark source of trip
    public void btnOnclickSelectSource(View view){
        if(firstPoint == null){
            firstPoint = new GeoPoint(map.getMapCenter());
            firstMarker = new Marker(map);
            firstMarker.setId("firstMarker");
            firstMarker.setPosition(firstPoint);
            firstMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            @SuppressLint("UseCompatLoadingForDrawables") Drawable marker = getResources().getDrawable(R.drawable.marker);
            firstMarker.setIcon(marker);
            map.getOverlays().clear();
            map.getOverlays().add(firstMarker);
            map.canZoomOut();

            //ui operations
            Button btnSource = view.findViewById(R.id.btnSelectSource);
            btnSource.setText(R.string.set_destenation);
        }
        else if(secondPoint == null){
            secondPoint = new GeoPoint(map.getMapCenter());
            secondMarker = new Marker(map);
            secondMarker.setId("secondMarker");
            secondMarker.setPosition(secondPoint);
            secondMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            @SuppressLint("UseCompatLoadingForDrawables") Drawable marker = getResources().getDrawable(R.drawable.marker);
            secondMarker.setIcon(marker);
            map.getOverlays().add(secondMarker);
            new Thread(new Runnable() {
                public void run() {
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            app.BASE_API_URL + "client/trip/calc",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        String errorCode = jsonObject.getString("error");
                                        if (errorCode.equals("0")) {
                                            //save number in sharedata
                                            String calcID = jsonObject.getJSONObject("data").getString("id");
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                                shareData.getShareData().edit().putString(String.valueOf(shareData.LAST_CALC_ID), calcID).apply();
                                            }
                                            else{
                                                app.failToast("android version is too low... we can not service");
                                            }
                                            //save sharedata
                                            shareData.getShareData().edit().putString(String.valueOf(shareData.P1LAT), String.valueOf(firstPoint.getLatitude())).apply();
                                            shareData.getShareData().edit().putString(String.valueOf(shareData.P2LAT), String.valueOf(secondPoint.getLatitude())).apply();
                                            //shareData.getShareData().edit().putString(String.valueOf(shareData.P3LAT), String.valueOf(thirdPoint.getLatitude())).apply();
                                            shareData.getShareData().edit().putString(String.valueOf(shareData.P1LONG), String.valueOf(firstPoint.getLongitude())).apply();
                                            shareData.getShareData().edit().putString(String.valueOf(shareData.P2LONG), String.valueOf(secondPoint.getLongitude())).apply();
                                            //shareData.getShareData().edit().putString(String.valueOf(shareData.P3LONG), String.valueOf(thirdPoint.getLongitude())).apply();
                                            //open accept code form
                                            Intent intent = new Intent(MainActivity.this, FrmShowPrice.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    app.failToast(getString(R.string.network_error));
                                }
                            }

                    ) {
                        @NonNull
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("p1lat", String.valueOf(firstPoint.getLatitude()));
                            params.put("p1long", String.valueOf(firstPoint.getLongitude()));
                            params.put("p2lat", String.valueOf(secondPoint.getLatitude()));
                            params.put("p2long", String.valueOf(secondPoint.getLongitude()));
                            params.put("type", "basic");
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("X-AUTH-TOKEN", shareData.getShareData().getString(shareData.AUTHKEY,""));
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
            }).start();
        }

    }
}
