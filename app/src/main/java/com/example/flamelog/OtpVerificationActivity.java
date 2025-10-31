package com.example.flamelog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    EditText[] otpBoxes = new EditText[6];
    GridLayout keypad;
    Button verifyButton;

    int currentIndex = 0; // which OTP box to fill next

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpBoxes[0] = findViewById(R.id.otp_1);
        otpBoxes[1] = findViewById(R.id.otp_2);
        otpBoxes[2] = findViewById(R.id.otp_3);
        otpBoxes[3] = findViewById(R.id.otp_4);
        otpBoxes[4] = findViewById(R.id.otp_5);
        otpBoxes[5] = findViewById(R.id.otp_6);
        verifyButton = findViewById(R.id.verify_button);
        keypad = findViewById(R.id.otp_number_pad);

        setupNumberPad();
        setupVerifyButton();
    }

    private void setupNumberPad() {
        // Loop through all buttons inside the GridLayout
        for (int i = 0; i < keypad.getChildCount(); i++) {
            if (keypad.getChildAt(i) instanceof Button) {
                Button button = (Button) keypad.getChildAt(i);

                // Set listener for each number button
                button.setOnClickListener(v -> {
                    String value = button.getText().toString();
                    if (!value.isEmpty() && currentIndex < otpBoxes.length) {
                        otpBoxes[currentIndex].setText(value);
                        currentIndex++;
                    }
                });
            }
        }

        // Optionally add long click to delete last digit
        keypad.setOnLongClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                otpBoxes[currentIndex].setText("");
            }
            return true;
        });
    }

    private void setupVerifyButton() {
        verifyButton.setOnClickListener(v -> {
            StringBuilder enteredOtp = new StringBuilder();
            for (EditText box : otpBoxes) {
                enteredOtp.append(box.getText().toString());
            }

            if (enteredOtp.length() == 6) {
                // ✅ Simulate verification
                Toast.makeText(this, "Code Verified: " + enteredOtp, Toast.LENGTH_LONG).show();
                // TODO: Add real verification logic (Firebase Auth, etc.)
            } else {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
