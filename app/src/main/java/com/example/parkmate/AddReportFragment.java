package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.parkmate.databinding.FragmentAddReportBinding;

import java.util.Locale;

public class AddReportFragment extends Fragment {

    private FragmentAddReportBinding binding;

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
        binding.btnStatusAvailable.setOnClickListener(v -> setStatus("AVAILABLE"));
        binding.btnStatusTaken.setOnClickListener(v -> setStatus("TAKEN"));

        // שמירה
        binding.btnSaveReport.setOnClickListener(v -> {
            String address = textOf(binding.etAddress);
            String status = textOf(binding.etStatus).toUpperCase(Locale.ROOT);

            if (address.isEmpty()) address = "Unknown address";
            if (status.isEmpty()) status = "AVAILABLE";

            Bundle res = new Bundle();
            res.putString("address", address);
            res.putString("status", status);

            getParentFragmentManager().setFragmentResult("add_report", res);
            NavHostFragment.findNavController(AddReportFragment.this).popBackStack();
        });
    }

    private void setStatus(String status) {
        binding.etStatus.setText(status);
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
