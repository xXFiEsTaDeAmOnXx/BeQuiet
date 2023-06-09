package com.example.bequiet.view.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.bequiet.R;
import com.example.bequiet.view.CircleOverlay;
import com.example.bequiet.view.GPSCoordinateSelectedListener;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectAreaFragment extends Fragment implements MapListener {

    private static final String KEY_LATITUDE = "LATITUDE";
    private static final String KEY_LONGITUDE = "LONGITUDE";
    private static final String KEY_ZOOM = "ZOOM";
    private static final String KEY_RADIUS = "RADIUS";

    private static final String KEY_DISABLE_MOVEMENT = "DISABLE_MVMNT";

    private MapView map;
    private CircleOverlay mCircleOverlay;
    private double lat = 47.8127457112777;
    private double lon = 9.656508679012063;

    private float radius = 40.0f;
    private int zoom = 20;
    private boolean disableControlls = false;

    public SelectAreaFragment() {
        // Required empty public constructor
    }

    public SelectAreaFragment(double lat, double longitude, float radius, int zoom, boolean disableControlls) {
        this.lat = lat;
        this.lon = longitude;
        this.disableControlls = disableControlls;
        this.radius = radius;
        this.zoom = zoom;
    }

    private GPSCoordinateSelectedListener gpsCoordinateSelectedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble(KEY_LATITUDE);
            lon = getArguments().getDouble(KEY_LONGITUDE);
            zoom = getArguments().getInt(KEY_ZOOM);
            radius = getArguments().getFloat(KEY_RADIUS);
            disableControlls = getArguments().getBoolean(KEY_DISABLE_MOVEMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_area, container, false);
    }

    // It is intentional that we ignore Clicks here!
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        map = (MapView) view.findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(!disableControlls);

        if (disableControlls) {
            // Disable all touch interaction
            map.setOnTouchListener((v, event) -> true);
        } else {
            // we are in selection mode
            // set the location to the last known gps location to make selection easier
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    this.lat = lastKnownLocation.getLatitude();
                    this.lon = lastKnownLocation.getLongitude();
                }
            }
        }


        mCircleOverlay = new CircleOverlay(
                map.getContext(),
                map.getProjection().metersToPixels(radius, lat, zoom),
                map.getProjection().metersToPixels(1, lat, zoom));
        map.getOverlayManager().add(mCircleOverlay);
        map.addMapListener(this);


        IMapController mapController = map.getController();
        mapController.setZoom(zoom);
        mapController.setCenter(new GeoPoint(lat, lon)); //geographic point of Hochschule Ravensbrug-Weingarten
        super.onViewCreated(view, savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDetach(); // Broadcastlisteners in Library need to be unregistered otherwise they crash the app.
    }

    public void setGpsCoordinateSelectedListener(GPSCoordinateSelectedListener gpsCoordinateSelectedListener) {
        this.gpsCoordinateSelectedListener = gpsCoordinateSelectedListener;
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        IGeoPoint center = map.getMapCenter();
        if (this.gpsCoordinateSelectedListener != null)
            this.gpsCoordinateSelectedListener.onGPSCoordinateSelected(center.getLatitude(), center.getLongitude(), (int) mCircleOverlay.getRadiusInMeter(), map.getZoomLevel());
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        mCircleOverlay.setReferenceScale(map.getProjection().metersToPixels(1, lat, map.getZoomLevel()));
        Log.i("Meter", "" + mCircleOverlay.getRadiusInMeter()); //radius scales
        return false;
    }
}

