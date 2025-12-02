package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Link UI
        ImageView logoImage = findViewById(R.id.logoImage);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        // Load neon pulse animation
        Animation neonPulse = AnimationUtils.loadAnimation(this, R.anim.neon_pulse);



        // Start animation on button
        btnGetStarted.startAnimation(neonPulse);

        // Click to open MainActivity
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
