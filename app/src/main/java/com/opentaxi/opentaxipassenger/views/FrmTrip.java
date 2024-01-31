package com.opentaxi.opentaxipassenger.views;

import static com.opentaxi.opentaxipassenger.app.application.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;

import com.opentaxi.opentaxipassenger.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.OneFingerZoomOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class FrmTrip extends AppCompatActivity {

    private MapView map = null;
    MyLocationNewOverlay mLocationOverlay;
    OneFingerZoomOverlay mOneFingerZoomOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        setContentView(R.layout.activity_frm_trip);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //add zoom with multi touch
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        //map controller
        IMapController mapController = map.getController();
        mapController.setZoom(16);

        //disable map replication in x and y
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);

        //My Location
        //note you have handle the permissions yourself, the overlay did not do it for you
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        // support for one finger zoom
        mOneFingerZoomOverlay = new OneFingerZoomOverlay();
        map.getOverlays().add(this.mOneFingerZoomOverlay);

    }
}