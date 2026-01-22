package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import com.google.android.material.slider.Slider;

import com.example.parkmate.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private final ArrayList<ParkingReport> demo = new ArrayList<>();
    private ParkingReportAdapter adapter;

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

        // Demo data
        demo.clear();
        demo.add(new ParkingReport("1", "Herzl 12", "AVAILABLE"));
        demo.add(new ParkingReport("2", "Ben Gurion 5", "TAKEN"));
        demo.add(new ParkingReport("3", "Dizengoff 100", "AVAILABLE"));

        // Adapter
        adapter = new ParkingReportAdapter(demo, this::openDetails);
        binding.rvReports.setAdapter(adapter);

        // Listen for result from AddReportFragment
        getParentFragmentManager().setFragmentResultListener("add_report", this, (requestKey, result) -> {
            String address = result.getString("address", "");
            String status = result.getString("status", "AVAILABLE");

            demo.add(new ParkingReport(String.valueOf(demo.size() + 1), address, status));
            adapter.notifyItemInserted(demo.size() - 1);
        });

        // Find parking nearby -> opens dialog
        binding.btnFindNearby.setOnClickListener(v -> showFindParkingDialog());
    }

    private void openDetails(ParkingReport report) {
        Bundle b = new Bundle();
        b.putString("address", report.address);
        b.putString("status", report.status);
        b.putString("reporterName", "Amiram"); // דמו - אחר כך מהשרת/Firebase
        b.putFloat("rating", 4.5f);            // דמו

        NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_reportDetailsFragment, b);
    }

    private void showFindParkingDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_find_parking, null, false);
        // --- Distance UI ---
        android.widget.TextView tvDistanceValue = dialogView.findViewById(R.id.tvDistanceValue);
        com.google.android.material.slider.Slider sliderDistance = dialogView.findViewById(R.id.sliderDistance);

       // אם את רוצה לשמור את הבחירה לפעם הבאה:
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE);

       // ערך ברירת מחדל 10 ק"מ
        float savedKm = prefs.getFloat("pref_distance_km", 10f);

        // אתחול UI לפי הערך השמור
        sliderDistance.setValue(savedKm);
        tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", savedKm));

       // עדכון בזמן אמת כשמזיזים את הסליידר
        sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            tvDistanceValue.setText(String.format(java.util.Locale.US, "%.0f", value));

            // לשמור ישר (אופציונלי)
            prefs.edit().putFloat("pref_distance_km", value).apply();
        });

        SearchView sv = dialogView.findViewById(R.id.svSearch);
        RecyclerView rvDialog = dialogView.findViewById(R.id.rvAvailable);

        rvDialog.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<ParkingReport> filtered = new ArrayList<>(demo);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        ParkingReportAdapter dialogAdapter = new ParkingReportAdapter(filtered, report -> {
            dialog.dismiss();
            openDetails(report);
        });
        rvDialog.setAdapter(dialogAdapter);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                String q = (newText == null) ? "" : newText.toLowerCase(Locale.ROOT).trim();

                filtered.clear();
                if (q.isEmpty()) {
                    filtered.addAll(demo);
                } else {
                    for (ParkingReport r : demo) {
                        String address = (r.address == null) ? "" : r.address.toLowerCase(Locale.ROOT);
                        String status = (r.status == null) ? "" : r.status.toLowerCase(Locale.ROOT);

                        if (address.contains(q) || status.contains(q)) {
                            filtered.add(r);
                        }
                    }
                }
                dialogAdapter.notifyDataSetChanged();
                return true;
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
