package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneAuthActivity extends AppCompatActivity {

    EditText phoneNumberInput;
    Button sendCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        phoneNumberInput = findViewById(R.id.phone_number_input);
        sendCodeButton = findViewById(R.id.send_verification_button);

        sendCodeButton.setOnClickListener(v -> {
            String phone = phoneNumberInput.getText().toString().trim();

            if (phone.isEmpty() || phone.length() < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            } else {
                // Normally you'd send an OTP here (Firebase/Auth API)
                Toast.makeText(this, "OTP sent to " + phone, Toast.LENGTH_SHORT).show();

                // Go to OTP verification screen
                Intent intent = new Intent(PhoneAuthActivity.this, OtpVerificationActivity.class);
                intent.putExtra("phone_number", phone);
                startActivity(intent);
            }
        });
    }
}
