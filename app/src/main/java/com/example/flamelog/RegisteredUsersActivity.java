package com.example.flamelog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisteredUsersActivity extends AppCompatActivity {

    private GridLayout gridPendingUsers, gridRegisteredUsers;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_users);

        gridPendingUsers = findViewById(R.id.gridPendingUsers);
        gridRegisteredUsers = findViewById(R.id.gridRegisteredUsers);

        userRef = FirebaseDatabase.getInstance().getReference("UserProfile");

        loadUsers();
    }

    private void loadUsers() {
        gridPendingUsers.removeAllViews();
        gridRegisteredUsers.removeAllViews();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gridPendingUsers.removeAllViews();
                gridRegisteredUsers.removeAllViews();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String email = userSnap.child("email").getValue(String.class);
                    String role = userSnap.child("role").getValue(String.class);
                    Boolean approved = userSnap.child("approved").getValue(Boolean.class);

                    if (email == null) continue;

                    if (approved != null && !approved) {
                        addPendingUser(uid, email);
                    }

                    if (approved != null && approved
                            && (role == null || !role.equalsIgnoreCase("admin"))) {
                        addRegisteredUser(uid, email);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RegisteredUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPendingUser(String uid, String email) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(12, 12, 12, 12);

        TextView tvEmail = new TextView(this);
        tvEmail.setText(email);
        tvEmail.setTextColor(0xFFFFD67C);
        tvEmail.setTextSize(16);
        tvEmail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnAccept = new Button(this);
        btnAccept.setText("ACCEPT");
        btnAccept.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
        btnAccept.setTextColor(0xFFFFFFFF);
        btnAccept.setOnClickListener(v -> {
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", email);
            profile.put("approved", true);
            profile.put("role", "user");
            userRef.child(uid).setValue(profile);
            Toast.makeText(this, "User approved", Toast.LENGTH_SHORT).show();
        });

        Button btnReject = new Button(this);
        btnReject.setText("REJECT");
        btnReject.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
        btnReject.setTextColor(0xFFFFFFFF);
        btnReject.setOnClickListener(v -> {
            userRef.child(uid).removeValue();
            Toast.makeText(this, "User rejected and deleted", Toast.LENGTH_SHORT).show();
        });

        row.addView(tvEmail);
        row.addView(btnAccept);
        row.addView(btnReject);

        gridPendingUsers.addView(row);
    }

    private void addRegisteredUser(String uid, String email) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(12, 12, 12, 12);

        TextView tvEmail = new TextView(this);
        tvEmail.setText(email);
        tvEmail.setTextColor(0xFFFFD67C);
        tvEmail.setTextSize(16);
        tvEmail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnRemove = new Button(this);
        btnRemove.setText("REMOVE");
        btnRemove.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
        btnRemove.setTextColor(0xFFFFFFFF);
        btnRemove.setOnClickListener(v -> {
            userRef.child(uid).removeValue();
            Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
        });

        row.addView(tvEmail);
        row.addView(btnRemove);

        gridRegisteredUsers.addView(row);
    }
}

