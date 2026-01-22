package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.parkmate.databinding.FragmentReportDetailsBinding;
import com.google.android.material.chip.Chip;

import java.util.Locale;

public class ReportDetailsFragment extends Fragment {

    private FragmentReportDetailsBinding binding;

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

        String address = args.getString("address", "Unknown address");
        String status = args.getString("status", "AVAILABLE");

        String reporterName = args.getString("reporterName", "Anonymous");
        float rating = args.getFloat("rating", 0f);

        // Set UI
        binding.tvDetailsAddress.setText(address);
        binding.tvDetailsStatus.setText(status); // Chip

        binding.tvReporterName.setText(reporterName);
        binding.rbReportRating.setRating(rating);
        binding.tvRatingValue.setText(String.format(Locale.US, "%.1f/5", rating));

        binding.btnRateReport.setOnClickListener(v -> openRateDialog());
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

                    binding.rbReportRating.setRating(newRating);
                    binding.tvRatingValue.setText(String.format(Locale.US, "%.1f/5", newRating));

                    // TODO later: send to Firebase / server
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
