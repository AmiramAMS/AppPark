package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.slider.Slider;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.User;
import com.example.parkmate.data.repository.AuthRepository;
import com.example.parkmate.data.repository.ParkingReportRepository;
import com.example.parkmate.data.repository.UserRepository;
import com.example.parkmate.databinding.FragmentHomeBinding;

import java.util.ArrayList;
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

        // Adapter
        adapter = new ParkingReportAdapter(demo, this::openDetails);
        binding.rvReports.setAdapter(adapter);

        // Find parking nearby -> opens dialog
        binding.btnFindNearby.setOnClickListener(v -> showFindParkingDialog());
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

        ParkingReportAdapter dialogAdapter = new ParkingReportAdapter(filtered, report -> {
            dialog.dismiss();
            openDetails(report);
        });
        rvDialog.setAdapter(dialogAdapter);

        final String[] selectedPlaceType = new String[]{"parking_lot"};
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

        int radiusKm = Math.round(sliderDistance.getValue());
        parkingReportRepository.getAvailableReportsNear(userLat, userLng, radiusKm, placeType, new Callback<ArrayList<com.example.parkmate.data.model.ParkingReport>>() {
            @Override
            public void onSuccess(ArrayList<com.example.parkmate.data.model.ParkingReport> reports) {
                if (!isAdded()) return;
                target.clear();
                for (com.example.parkmate.data.model.ParkingReport r : reports) {
                    String address = (r.getAddress() == null || r.getAddress().trim().isEmpty())
                            ? "Unknown address"
                            : r.getAddress().trim();
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
        parkingReportRepository.getReports(new Callback<ArrayList<com.example.parkmate.data.model.ParkingReport>>() {
            @Override
            public void onSuccess(ArrayList<com.example.parkmate.data.model.ParkingReport> reports) {
                if (!isAdded() || binding == null) return;

                demo.clear();

                for (com.example.parkmate.data.model.ParkingReport r : reports) {
                    String address = (r.getAddress() == null || r.getAddress().trim().isEmpty())
                            ? "Unknown address"
                            : r.getAddress().trim();
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
