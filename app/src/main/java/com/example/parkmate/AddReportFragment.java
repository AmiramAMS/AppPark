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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.repository.ParkingReportRepository;
import com.example.parkmate.databinding.FragmentAddReportBinding;

import java.util.Locale;

public class AddReportFragment extends Fragment {

    private FragmentAddReportBinding binding;
    private final ParkingReportRepository parkingReportRepository = new ParkingReportRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // כפתורי סטטוס -> ממלאים את ה-EditText של הסטטוס
        binding.btnStatusAvailable.setOnClickListener(v -> setStatus("available"));
        binding.btnStatusTaken.setOnClickListener(v -> setStatus("occupied"));

        binding.btnPlaceParkingLot.setOnClickListener(v -> setPlaceType("parking_lot"));
        binding.btnPlaceEntertainment.setOnClickListener(v -> setPlaceType("entertainment"));

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            fillLocationFromGps();
        }

        // שמירה
        binding.btnSaveReport.setOnClickListener(v -> {
            String address = textOf(binding.etAddress);
            String status = textOf(binding.etStatus);
            String placeType = textOf(binding.etPlaceType);
            String latText = textOf(binding.etLatitude);
            String lngText = textOf(binding.etLongitude);

            if (address.isEmpty()) {
                binding.etAddress.setError("Please enter address");
                return;
            }
            if (status.isEmpty()) {
                binding.etStatus.setError("Please select status");
                return;
            }
            if (placeType.isEmpty()) {
                binding.etPlaceType.setError("Please select place type");
                return;
            }

            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(latText);
                longitude = Double.parseDouble(lngText);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Please enter valid coordinates", Toast.LENGTH_LONG).show();
                return;
            }

            binding.btnSaveReport.setEnabled(false);
            parkingReportRepository.addReport(latitude, longitude, address, placeType, status, new Callback<String>() {
                @Override
                public void onSuccess(String reportId) {
                    if (!isAdded() || binding == null) return;
                    binding.btnSaveReport.setEnabled(true);
                    NavHostFragment.findNavController(AddReportFragment.this).popBackStack();
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    binding.btnSaveReport.setEnabled(true);
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setStatus(String status) {
        binding.etStatus.setText(status);
    }

    private void setPlaceType(String placeType) {
        binding.etPlaceType.setText(placeType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillLocationFromGps();
        }
    }

    private void fillLocationFromGps() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && isAdded() && binding != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                UserLocationHolder.set(lat, lng);
                binding.etLatitude.setText(String.format(Locale.US, "%.6f", lat));
                binding.etLongitude.setText(String.format(Locale.US, "%.6f", lng));
            }
        });
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
