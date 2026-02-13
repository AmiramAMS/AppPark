package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.User;
import com.example.parkmate.data.model.UserStats;
import com.example.parkmate.data.repository.AuthRepository;
import com.example.parkmate.data.repository.ParkingReportRepository;
import com.example.parkmate.data.repository.UserRepository;
import com.example.parkmate.databinding.FragmentProfileBinding;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ParkingReportRepository parkingReportRepository = new ParkingReportRepository();

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

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            binding.tvUserName.setText("Guest");
            binding.tvUserEmail.setText("Not signed in");
            binding.tvProfileEmail.setText("Not signed in");
            binding.tvReportsCount.setText("0");
            binding.tvProfileReportsCount.setText("0");
            binding.tvLikesReceived.setText("0");
            binding.tvProfileLikesCount.setText("0");
        } else {
            userRepository.getUser(uid, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (!isAdded() || binding == null) return;

                    String email = user.getEmail() == null ? "" : user.getEmail();
                    String fullName = user.getFullName() == null ? "" : user.getFullName().trim();
                    if (fullName.isEmpty()) {
                        fullName = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Guest";
                    }

                    // אצלך זה TextInputEditText, לכן setText עובד
                    binding.tvUserName.setText(fullName);

                    // יש לך שני מקומות שמציגים אימייל - נמלא את שניהם כדי שייראה עקבי
                    binding.tvUserEmail.setText(email);
                    binding.tvProfileEmail.setText(email);
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            parkingReportRepository.getUserStats(uid, new Callback<UserStats>() {
                @Override
                public void onSuccess(UserStats stats) {
                    if (!isAdded() || binding == null) return;
                    String reportsCount = String.valueOf(stats.getReportsCount());
                    binding.tvReportsCount.setText(reportsCount);
                    binding.tvProfileReportsCount.setText(reportsCount);
                    binding.tvProfileLikesCount.setText(String.valueOf(stats.getTotalLikesReceived()));
                    binding.tvLikesReceived.setText(String.format(Locale.US, "%.1f", stats.getRatingAverageReceived()));
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        // (אופציונלי) כפתור עיפרון: להפוך את השם לניתן לעריכה/נעילה
        binding.btnEditProfile.setOnClickListener(v -> {
            boolean enabled = binding.tvUserName.isEnabled();
            binding.tvUserName.setEnabled(!enabled);

            if (!enabled) {
                binding.tvUserName.requestFocus();
                binding.tvUserName.setSelection(binding.tvUserName.getText().length());
            } else {
                // כשנועלים שוב - לשמור את השם החדש ל-Firestore
                String nameInput = binding.tvUserName.getText() == null ? "" : binding.tvUserName.getText().toString().trim();
                final String newName = nameInput.isEmpty() ? "Guest" : nameInput;

                String currentUid = authRepository.getCurrentUid();
                if (currentUid == null) return;

                binding.btnEditProfile.setEnabled(false);
                userRepository.updateFullName(currentUid, newName, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        if (!isAdded() || binding == null) return;
                        binding.btnEditProfile.setEnabled(true);
                        binding.tvUserName.setText(newName);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded() || binding == null) return;
                        binding.btnEditProfile.setEnabled(true);
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
