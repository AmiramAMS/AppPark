package com.example.parkmate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.slider.Slider;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.User;
import com.example.parkmate.data.repository.AuthRepository;
import com.example.parkmate.data.repository.ParkingReportRepository;
import com.example.parkmate.data.repository.UserRepository;
import com.example.parkmate.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private final ArrayList<ParkingReport> demo = new ArrayList<>();
    private ParkingReportAdapter adapter;
    private final AuthRepository authRepository = new AuthRepository();
    private final ParkingReportRepository parkingReportRepository = new ParkingReportRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Profile button in toolbar
        binding.btnProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_homeFragment_to_profileFragment)
        );

        // Floating Action Button for adding new report
        binding.fabAddReport.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_homeFragment_to_addReportFragment)
        );

        // RecyclerView
        binding.rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter (main list: mark as taken updates DB and local status)
        adapter = new ParkingReportAdapter(demo, this::openDetails, report -> {
            parkingReportRepository.updateReportStatus(report.id, "occupied", new Callback<Void>() {
                @Override
                public void onSuccess(Void ignored) {
                    if (!isAdded() || binding == null) return;
                    report.status = "occupied";
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        binding.rvReports.setAdapter(adapter);

        // Find parking nearby -> opens dialog
        binding.btnFindNearby.setOnClickListener(v -> showFindParkingDialog());

        requestLocationPermissionAndFetch();
    }

    private static final int REQUEST_CODE_LOCATION = 1001;

    private void requestLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            return;
        }
        fetchAndStoreLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndStoreLocation();
        }
    }

    private void fetchAndStoreLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && isAdded()) {
                UserLocationHolder.set(location.getLatitude(), location.getLongitude());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }

    private void openDetails(ParkingReport report) {
        Bundle b = new Bundle();
        b.putString("reportId", report.id);

        NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_reportDetailsFragment, b);
    }

    private void showFindParkingDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_find_parking, null, false);
        // --- Distance UI ---
        android.widget.TextView tvDistanceValue = dialogView.findViewById(R.id.tvDistanceValue);
        com.google.android.material.slider.Slider sliderDistance = dialogView.findViewById(R.id.sliderDistance);
        com.google.android.material.textfield.TextInputEditText etUserLat = dialogView.findViewById(R.id.etUserLatitude);
        com.google.android.material.textfield.TextInputEditText etUserLng = dialogView.findViewById(R.id.etUserLongitude);

        com.google.android.material.button.MaterialButton btnFilterParkingLot = dialogView.findViewById(R.id.btnFilterParkingLot);
        com.google.android.material.button.MaterialButton btnFilterEntertainment = dialogView.findViewById(R.id.btnFilterEntertainment);

        if (UserLocationHolder.hasLocation()) {
            etUserLat.setText(String.format(Locale.US, "%.6f", UserLocationHolder.getLat()));
            etUserLng.setText(String.format(Locale.US, "%.6f", UserLocationHolder.getLng()));
        }

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            float fallbackKm = 10f;
            sliderDistance.setValue(fallbackKm);
            tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", fallbackKm));
        } else {
            userRepository.getUser(uid, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (!isAdded()) return;
                    float km = (float) user.getRadiusPreference();
                    sliderDistance.setValue(km);
                    tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", km));
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    float fallbackKm = 10f;
                    sliderDistance.setValue(fallbackKm);
                    tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", fallbackKm));
                }
            });
        }

       // עדכון בזמן אמת כשמזיזים את הסליידר
        sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", value));

            if (!fromUser) return;

            String currentUid = authRepository.getCurrentUid();
            if (currentUid == null) return;

            // Save radiusPreference to Firestore (int).
            userRepository.updateRadiusPreference(currentUid, Math.round(value), new Callback<Void>() {
                @Override
                public void onSuccess(Void ignored) {
                    // no-op
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        SearchView sv = dialogView.findViewById(R.id.svSearch);
        RecyclerView rvDialog = dialogView.findViewById(R.id.rvAvailable);

        rvDialog.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        ArrayList<ParkingReport> filtered = new ArrayList<>();

        final ParkingReportAdapter[] adapterHolder = new ParkingReportAdapter[1];
        adapterHolder[0] = new ParkingReportAdapter(filtered, report -> {
            dialog.dismiss();
            openDetails(report);
        }, report -> {
            parkingReportRepository.updateReportStatus(report.id, "occupied", new Callback<Void>() {
                @Override
                public void onSuccess(Void ignored) {
                    if (!isAdded()) return;
                    filtered.remove(report);
                    adapterHolder[0].notifyDataSetChanged();
                }
                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        rvDialog.setAdapter(adapterHolder[0]);

        final String[] selectedPlaceType = new String[]{"parking_lot"};
        ParkingReportAdapter dialogAdapter = adapterHolder[0];
        btnFilterParkingLot.setOnClickListener(v -> {
            selectedPlaceType[0] = "parking_lot";
            loadAvailableNearby(filtered, dialogAdapter, etUserLat, etUserLng, sliderDistance, selectedPlaceType[0]);
        });
        btnFilterEntertainment.setOnClickListener(v -> {
            selectedPlaceType[0] = "entertainment";
            loadAvailableNearby(filtered, dialogAdapter, etUserLat, etUserLng, sliderDistance, selectedPlaceType[0]);
        });

        loadAvailableNearby(filtered, dialogAdapter, etUserLat, etUserLng, sliderDistance, selectedPlaceType[0]);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                String q = (newText == null) ? "" : newText.toLowerCase(Locale.ROOT).trim();

                dialogAdapter.notifyDataSetChanged();
                return true;
            }
        });

        dialog.show();
    }

    private void loadAvailableNearby(
            ArrayList<ParkingReport> target,
            ParkingReportAdapter adapter,
            com.google.android.material.textfield.TextInputEditText etUserLat,
            com.google.android.material.textfield.TextInputEditText etUserLng,
            Slider sliderDistance,
            String placeType
    ) {
        String latText = etUserLat.getText() == null ? "" : etUserLat.getText().toString().trim();
        String lngText = etUserLng.getText() == null ? "" : etUserLng.getText().toString().trim();
        if (latText.isEmpty() || lngText.isEmpty()) {
            target.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        double userLat;
        double userLng;
        try {
            userLat = Double.parseDouble(latText);
            userLng = Double.parseDouble(lngText);
        } catch (Exception e) {
            target.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        final int radiusKm = Math.round(sliderDistance.getValue());
        parkingReportRepository.getReports(new Callback<ArrayList<com.example.parkmate.data.model.ParkingReport>>() {
            @Override
            public void onSuccess(ArrayList<com.example.parkmate.data.model.ParkingReport> reports) {
                if (!isAdded()) return;
                float[] results = new float[1];
                List<com.example.parkmate.data.model.ParkingReport> filtered = new ArrayList<>();
                for (com.example.parkmate.data.model.ParkingReport r : reports) {
                    if (!"available".equals(r.getStatus())) continue;
                    if (!placeType.equals(r.getPlaceType() != null ? r.getPlaceType().trim() : "")) continue;
                    android.location.Location.distanceBetween(
                            userLat, userLng,
                            r.getLatitude(), r.getLongitude(),
                            results);
                    if (results[0] / 1000f <= radiusKm) filtered.add(r);
                }
                Collections.sort(filtered, new Comparator<com.example.parkmate.data.model.ParkingReport>() {
                    @Override
                    public int compare(com.example.parkmate.data.model.ParkingReport a, com.example.parkmate.data.model.ParkingReport b) {
                        int byStars = Double.compare(b.getRatingAverage(), a.getRatingAverage());
                        if (byStars != 0) return byStars;
                        long at = a.getCreatedAt() == null ? 0 : a.getCreatedAt().getSeconds();
                        long bt = b.getCreatedAt() == null ? 0 : b.getCreatedAt().getSeconds();
                        return Long.compare(bt, at);
                    }
                });
                target.clear();
                for (com.example.parkmate.data.model.ParkingReport r : filtered) {
                    String address = (r.getAddress() == null || r.getAddress().trim().isEmpty())
                            ? "Unknown address" : r.getAddress().trim();
                    String status = r.getStatus() == null ? "" : r.getStatus();
                    target.add(new ParkingReport(r.getId(), address, status));
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadReports() {
        String uid = authRepository.getCurrentUid();
        if (uid != null) {
            userRepository.getUser(uid, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (!isAdded()) return;
                    doLoadReports((float) user.getRadiusPreference());
                }
                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    doLoadReports(10f);
                }
            });
        } else {
            doLoadReports(10f);
        }
    }

    private void doLoadReports(float radiusKm) {
        parkingReportRepository.getReports(new Callback<ArrayList<com.example.parkmate.data.model.ParkingReport>>() {
            @Override
            public void onSuccess(ArrayList<com.example.parkmate.data.model.ParkingReport> reports) {
                if (!isAdded() || binding == null) return;

                double userLat = UserLocationHolder.hasLocation() ? UserLocationHolder.getLat() : Double.NaN;
                double userLng = UserLocationHolder.hasLocation() ? UserLocationHolder.getLng() : Double.NaN;

                List<com.example.parkmate.data.model.ParkingReport> filtered = new ArrayList<>();
                float[] results = new float[1];
                for (com.example.parkmate.data.model.ParkingReport r : reports) {
                    if (Double.isNaN(userLat) || Double.isNaN(userLng)) {
                        filtered.add(r);
                        continue;
                    }
                    android.location.Location.distanceBetween(
                            userLat, userLng,
                            r.getLatitude(), r.getLongitude(),
                            results);
                    float distanceKm = results[0] / 1000f;
                    if (distanceKm <= radiusKm) filtered.add(r);
                }

                Collections.sort(filtered, new Comparator<com.example.parkmate.data.model.ParkingReport>() {
                    @Override
                    public int compare(com.example.parkmate.data.model.ParkingReport a, com.example.parkmate.data.model.ParkingReport b) {
                        int byStars = Double.compare(b.getRatingAverage(), a.getRatingAverage());
                        if (byStars != 0) return byStars;
                        long at = a.getCreatedAt() == null ? 0 : a.getCreatedAt().getSeconds();
                        long bt = b.getCreatedAt() == null ? 0 : b.getCreatedAt().getSeconds();
                        return Long.compare(bt, at);
                    }
                });

                demo.clear();
                for (com.example.parkmate.data.model.ParkingReport r : filtered) {
                    String address = (r.getAddress() == null || r.getAddress().trim().isEmpty())
                            ? "Unknown address" : r.getAddress().trim();
                    String status = r.getStatus() == null ? "" : r.getStatus();
                    demo.add(new ParkingReport(r.getId(), address, status));
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded() || binding == null) return;
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String shortId(String id) {
        if (id == null) return "";
        return id.length() <= 6 ? id : id.substring(0, 6);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
