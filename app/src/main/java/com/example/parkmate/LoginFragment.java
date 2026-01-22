package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.parkmate.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOGIN (דמו)
        binding.btnLogin.setOnClickListener(v -> {

            // 1️⃣ לוקחים את הנתונים מהשדות
            String email = binding.etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etEmail.setError("Please enter email");
                return;
            }

            // 2️⃣ שמירה מקומית (SharedPreferences)
            requireContext()
                    .getSharedPreferences("parkmate_prefs", 0)
                    .edit()
                    .putString("user_email", email)
                    .putString("user_name", email.split("@")[0]) // שם זמני
                    .apply();

            // 3️⃣ ניווט ל־Home
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_homeFragment);
        });


        // TOGGLE SIGN UP AREA
        binding.tvToggleSignup.setOnClickListener(v -> {
            if (binding.signupContainer.getVisibility() == View.VISIBLE) {
                binding.signupContainer.setVisibility(View.GONE);
                binding.tvToggleSignup.setText("Don't have an account? Create one");
            } else {
                binding.signupContainer.setVisibility(View.VISIBLE);
                binding.tvToggleSignup.setText("Already have an account? Hide sign up");
            }
        });

        // SIGN UP (דמו)
        binding.btnSignup.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String email = binding.etSignupEmail.getText().toString().trim();

            if (fullName.isEmpty()) fullName = "Guest";
            if (email.isEmpty()) email = "demo@mail.com";

            requireContext()
                    .getSharedPreferences("user_prefs", 0)
                    .edit()
                    .putString("username", fullName)
                    .putString("email", email)
                    .apply();
            // כאן בעתיד סטודנט 2 יחבר Firebase / שרת
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_homeFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
