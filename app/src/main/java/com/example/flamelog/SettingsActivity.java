package com.example.flamelog;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    private Switch switchBluetooth, switchNotifications, switchVibration;
    private Button btnConnectArduino;
    private TextView bluetoothStatus;
    private SeekBar seekBarSensitivity;

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize UI
        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchVibration = findViewById(R.id.switchVibration);
        btnConnectArduino = findViewById(R.id.btnConnectArduino);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        seekBarSensitivity = findViewById(R.id.seekBarSensitivity);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            bluetoothStatus.setText("Bluetooth not supported on this device");
            btnConnectArduino.setEnabled(false);
            return;
        }

        // 🔹 Handle Bluetooth Switch
        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) enableBluetooth();
            else disableBluetooth();
        });

        // 🔹 Handle Connect Button
        btnConnectArduino.setOnClickListener(v -> connectToArduino());
    }

    private void enableBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            bluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothStatus.setText("Bluetooth already on");
        }
    }

    private void disableBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            bluetoothStatus.setText("Bluetooth turned off");
            Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothStatus.setText("Bluetooth already off");
        }
    }

    private void connectToArduino() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothStatus.setText("Searching for Arduino...");
            // Example only: In real case, you'd discover paired devices here
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                // Replace with your Arduino device name if needed
                if (device.getName().contains("Arduino")) {
                    bluetoothStatus.setText("Connected to: " + device.getName());
                    Toast.makeText(this, "Connected to Arduino!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            bluetoothStatus.setText("No Arduino found");
        } else {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
