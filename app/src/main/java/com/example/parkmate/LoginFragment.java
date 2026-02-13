package com.example.parkmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.User;
import com.example.parkmate.data.repository.AuthRepository;
import com.example.parkmate.data.repository.UserRepository;
import com.example.parkmate.databinding.FragmentLoginBinding;
import com.google.firebase.Timestamp;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

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

        // LOGIN
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() == null ? "" : binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText() == null ? "" : binding.etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etEmail.setError("Please enter email");
                return;
            }
            if (password.isEmpty()) {
                binding.etPassword.setError("Please enter password");
                return;
            }

            binding.btnLogin.setEnabled(false);
            authRepository.signIn(email, password, new Callback<String>() {
                @Override
                public void onSuccess(String uid) {
                    if (!isAdded() || binding == null) return;
                    binding.btnLogin.setEnabled(true);
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_loginFragment_to_homeFragment);
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
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

        // SIGN UP
        binding.btnSignup.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText() == null ? "" : binding.etFullName.getText().toString().trim();
            String email = binding.etSignupEmail.getText() == null ? "" : binding.etSignupEmail.getText().toString().trim();
            String password = binding.etSignupPassword.getText() == null ? "" : binding.etSignupPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                binding.etFullName.setError("Please enter full name");
                return;
            }
            if (email.isEmpty()) {
                binding.etSignupEmail.setError("Please enter email");
                return;
            }
            if (password.isEmpty()) {
                binding.etSignupPassword.setError("Please enter password");
                return;
            }
            if (password.length() < 6) {
                binding.etSignupPassword.setError("Password must be at least 6 characters");
                return;
            }

            binding.btnSignup.setEnabled(false);
            authRepository.signUp(email, password, new Callback<String>() {
                @Override
                public void onSuccess(String uid) {
                    User user = new User(uid, email, fullName, 10, Timestamp.now());
                    userRepository.createUser(user, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void ignored) {
                            if (!isAdded() || binding == null) return;
                            binding.btnSignup.setEnabled(true);
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_homeFragment);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded() || binding == null) return;
                            binding.btnSignup.setEnabled(true);
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded() || binding == null) return;
                    binding.btnSignup.setEnabled(true);
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
