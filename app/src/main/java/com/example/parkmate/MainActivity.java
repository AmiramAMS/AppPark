package com.example.parkmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkmate.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Set the content view using binding
        setContentView(binding.getRoot());
    }
}
