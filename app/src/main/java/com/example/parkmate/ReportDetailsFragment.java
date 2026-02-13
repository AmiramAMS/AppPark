package com.example.parkmate;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.ParkingReport;
import com.example.parkmate.data.model.User;
import com.example.parkmate.data.repository.ParkingReportRepository;
import com.example.parkmate.data.repository.UserRepository;
import com.example.parkmate.databinding.FragmentReportDetailsBinding;

import java.util.Locale;

public class ReportDetailsFragment extends Fragment {

    private FragmentReportDetailsBinding binding;
    private final ParkingReportRepository parkingReportRepository = new ParkingReportRepository();
    private final UserRepository userRepository = new UserRepository();
    private String reportId;

    public ReportDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        reportId = args.getString("reportId", null);
        if (reportId == null || reportId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Missing report id", Toast.LENGTH_LONG).show();
            return;
        }

        loadReport();

        binding.btnRateReport.setOnClickListener(v -> openRateDialog());
    }

    private void loadReport() {
        parkingReportRepository.getReportById(reportId, new Callback<ParkingReport>() {
            @Override
            public void onSuccess(ParkingReport report) {
                if (!isAdded() || binding == null) return;

                String address = report.getAddress() == null ? "Unknown address" : report.getAddress();
                String status = report.getStatus() == null ? "" : report.getStatus();

                binding.tvDetailsAddress.setText(address);
                binding.tvDetailsStatus.setText(status);

                if (report.getCreatedAt() != null) {
                    String formatted = DateFormat.format("dd/MM/yyyy HH:mm", report.getCreatedAt().toDate()).toString();
                    binding.tvReportedTime.setText(formatted);
                }

                float avg = (float) report.getRatingAverage();
                binding.rbReportRating.setRating(avg);
                binding.tvRatingValue.setText(String.format(Locale.US, "%.1f/5", avg));

                loadReporterName(report.getOwnerId());
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded() || binding == null) return;
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadReporterName(String ownerId) {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            if (!isAdded() || binding == null) return;
            binding.tvReporterName.setText("Anonymous");
            return;
        }

        userRepository.getUser(ownerId, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded() || binding == null) return;
                String name = user.getFullName() == null ? "" : user.getFullName().trim();
                if (name.isEmpty()) name = "Anonymous";
                binding.tvReporterName.setText(name);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded() || binding == null) return;
                binding.tvReporterName.setText("Anonymous");
            }
        });
    }

    private void openRateDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_rate_report, null, false);

        android.widget.RatingBar rb = dialogView.findViewById(R.id.rbUserRating);

        new AlertDialog.Builder(requireContext())
                .setTitle("Rate report")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    float newRating = rb.getRating();
                    int value = Math.max(1, Math.min(5, Math.round(newRating)));

                    parkingReportRepository.rateReport(reportId, value, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void ignored) {
                            if (!isAdded() || binding == null) return;
                            loadReport();
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded() || binding == null) return;
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
