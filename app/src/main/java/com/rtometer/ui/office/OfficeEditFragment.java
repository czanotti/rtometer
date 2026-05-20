package com.rtometer.ui.office;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.rtometer.R;
import com.rtometer.data.db.Office;

import dagger.hilt.android.AndroidEntryPoint;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

import java.util.List;

@AndroidEntryPoint
public class OfficeEditFragment extends DialogFragment {

    private static final String ARG_OFFICE_ID = "office_id";
    private static final int MAX_RADIUS = 500;
    private static final double DEFAULT_LAT = 51.5074;
    private static final double DEFAULT_LON = -0.1278;

    public static OfficeEditFragment newInstance(long officeId) {
        OfficeEditFragment f = new OfficeEditFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_OFFICE_ID, officeId);
        f.setArguments(args);
        return f;
    }

    public static OfficeEditFragment newInstance() {
        return new OfficeEditFragment();
    }

    private OfficeSetupViewModel viewModel;
    private MapView mapView;
    private Marker pinMarker;
    private EditText etName;
    private SeekBar radiusSeekBar;
    private TextView radiusLabel;
    private TextView tvCoords;
    private double selectedLat = DEFAULT_LAT;
    private double selectedLon = DEFAULT_LON;
    private Office editingOffice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_office_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(OfficeSetupViewModel.class);

        etName = view.findViewById(R.id.etOfficeName);
        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        radiusLabel = view.findViewById(R.id.radiusLabel);
        tvCoords = view.findViewById(R.id.tvCoords);
        Button btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        radiusSeekBar.setMax(MAX_RADIUS);
        radiusSeekBar.setProgress(200);
        updateRadiusLabel(200);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                updateRadiusLabel(Math.max(progress, 50));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        tvCoords.setText(getString(R.string.office_coords_label, selectedLat, selectedLon));
        setupMap(view);
        loadEditingOffice();

        btnCurrentLocation.setOnClickListener(v -> useCurrentLocation());
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> dismiss());
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
    }

    private void loadEditingOffice() {
        if (getArguments() == null) return;
        long id = getArguments().getLong(ARG_OFFICE_ID, -1);
        if (id < 0) return;

        List<Office> offices = viewModel.offices.getValue();
        if (offices == null) return;
        for (Office o : offices) {
            if (o.id == id) {
                editingOffice = o;
                break;
            }
        }
        if (editingOffice == null) return;

        etName.setText(editingOffice.name);
        radiusSeekBar.setProgress(editingOffice.radiusMeters);
        updateRadiusLabel(editingOffice.radiusMeters);
        movePin(editingOffice.latitude, editingOffice.longitude);
    }

    private void movePin(double lat, double lon) {
        selectedLat = lat;
        selectedLon = lon;
        GeoPoint point = new GeoPoint(lat, lon);
        pinMarker.setPosition(point);
        mapView.getController().animateTo(point);
        mapView.invalidate();
        tvCoords.setText(getString(R.string.office_coords_label, lat, lon));
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

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError(getString(R.string.office_name_required));
            return;
        }
        int radius = Math.max(radiusSeekBar.getProgress(), 50);

        if (editingOffice != null) {
            editingOffice.name = name;
            editingOffice.latitude = selectedLat;
            editingOffice.longitude = selectedLon;
            editingOffice.radiusMeters = radius;
            viewModel.updateOffice(editingOffice);
        } else {
            viewModel.addOffice(name, selectedLat, selectedLon, radius);
        }
        dismiss();
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
}
