package com.example.nutri;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutri.Admin.AdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();

            checkIfAdmin(currentUser.getUid());
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
            }, 3000);
        }
    }

    private void checkIfAdmin(String uid) {
        DatabaseReference adminDatabase = FirebaseDatabase.getInstance().getReference("admins");

        adminDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent;
                if (dataSnapshot.exists()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        finish();
                    }, 3000);
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        finish();
                    }, 3000);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error checking admin status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
