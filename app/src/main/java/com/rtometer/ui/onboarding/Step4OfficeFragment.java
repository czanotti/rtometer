package com.rtometer.ui.onboarding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.rtometer.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class Step4OfficeFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private static final int MAX_RADIUS = 500;
    private static final double DEFAULT_LAT = 53.3478;
    private static final double DEFAULT_LON = -6.27591;
    private static final int DEFAULT_RADIUS = 200;

    private MapView mapView;
    private Marker pinMarker;
    private TextInputEditText etName;
    private SeekBar radiusSeekBar;
    private TextView radiusLabel;
    private TextView tvCoords;
    private double selectedLat = DEFAULT_LAT;
    private double selectedLon = DEFAULT_LON;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        return inflater.inflate(R.layout.fragment_step4_office, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etName = view.findViewById(R.id.etOfficeName);
        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        radiusLabel = view.findViewById(R.id.radiusLabel);
        tvCoords = view.findViewById(R.id.tvCoords);

        etName.setText("Kings Building");

        radiusSeekBar.setMax(MAX_RADIUS);
        radiusSeekBar.setProgress(DEFAULT_RADIUS);
        updateRadiusLabel(DEFAULT_RADIUS);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                updateRadiusLabel(Math.max(progress, 50));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        setupMap(view);
        view.findViewById(R.id.btnCurrentLocation).setOnClickListener(v -> useCurrentLocation());
    }

    private void setupMap(View view) {
        mapView = view.findViewById(R.id.officeMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        pinMarker = new Marker(mapView);
        pinMarker.setTitle(getString(R.string.office_map_pin_title));
        pinMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        GeoPoint start = new GeoPoint(selectedLat, selectedLon);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(start);
        pinMarker.setPosition(start);
        mapView.getOverlays().add(pinMarker);

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                movePin(p.getLatitude(), p.getLongitude());
                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        });
        mapView.getOverlays().add(0, eventsOverlay);

        updateCoordsLabel();
    }

    private void movePin(double lat, double lon) {
        selectedLat = lat;
        selectedLon = lon;
        GeoPoint point = new GeoPoint(lat, lon);
        pinMarker.setPosition(point);
        mapView.getController().animateTo(point);
        mapView.invalidate();
        updateCoordsLabel();
    }

    private void useCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), R.string.office_location_permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        LocationManager lm = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        Location loc = com.rtometer.gps.LocationUtils.pickBestLocation(
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER),
                lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        if (loc != null) {
            movePin(loc.getLatitude(), loc.getLongitude());
        } else {
            Toast.makeText(requireContext(), R.string.office_location_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoordsLabel() {
        tvCoords.setText(getString(R.string.office_coords_label, selectedLat, selectedLon));
    }

    private void updateRadiusLabel(int meters) {
        radiusLabel.setText(getString(R.string.office_radius_label, meters));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public boolean isValid() {
        return !etName.getText().toString().trim().isEmpty();
    }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        vm.setOfficeName(etName.getText().toString().trim());
        vm.setOfficeLat(selectedLat);
        vm.setOfficeLng(selectedLon);
        vm.setOfficeRadiusMeters(Math.max(radiusSeekBar.getProgress(), 50));
    }
}
