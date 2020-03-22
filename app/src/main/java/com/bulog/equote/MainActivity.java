package com.bulog.equote;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bulog.equote.databinding.ActivityMainBinding;
import com.bulog.equote.utils.SPService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SPService sharedPreferenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        sharedPreferenceService = new SPService(this);

        String token = sharedPreferenceService.getTokenFromSp();
        binding.TestTextView.setText("Logged in, Token : " + token);
    }
}
