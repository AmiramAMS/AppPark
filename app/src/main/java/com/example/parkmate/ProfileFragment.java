package com.example.parkmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parkmate.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", 0);

        String username = sp.getString("username", "Guest");
        String email = sp.getString("email", "demo@mail.com");

        // אצלך זה TextInputEditText, לכן setText עובד
        binding.tvUserName.setText(username);

        // יש לך שני מקומות שמציגים אימייל - נמלא את שניהם כדי שייראה עקבי
        binding.tvUserEmail.setText(email);
        binding.tvProfileEmail.setText(email);

        // (אופציונלי) כפתור עיפרון: להפוך את השם לניתן לעריכה/נעילה
        binding.btnEditProfile.setOnClickListener(v -> {
            boolean enabled = binding.tvUserName.isEnabled();
            binding.tvUserName.setEnabled(!enabled);

            if (!enabled) {
                binding.tvUserName.requestFocus();
                binding.tvUserName.setSelection(binding.tvUserName.getText().length());
            } else {
                // כשנועלים שוב - לשמור את השם החדש
                String newName = binding.tvUserName.getText() == null ? "" : binding.tvUserName.getText().toString().trim();
                if (newName.isEmpty()) newName = "Guest";
                sp.edit().putString("username", newName).apply();
                binding.tvUserName.setText(newName);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
