package com.example.nutri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private LinearLayout btnPersonalInfo, btnSetReminders, btnNutritionalReport, btnAddDevice, btnLogout;
    private TextView profile_name, profile_age, profile_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        profile_location = findViewById(R.id.profile_location);
        profile_age = findViewById(R.id.profile_age);
        profile_name = findViewById(R.id.profile_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firebase", "User not logged in");
            return;
        }
        String uid = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String age = dataSnapshot.child("age").getValue(String.class);
                String location = dataSnapshot.child("location").getValue(String.class);

                updateProfileUI(name, age, location);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ProfileActivity", "Error fetching data", databaseError.toException());
            }
        });
        btnPersonalInfo = findViewById(R.id.personalInfo);
        btnSetReminders = findViewById(R.id.setReminders);
        btnNutritionalReport = findViewById(R.id.nutritional_report);
        btnAddDevice = findViewById(R.id.add_device);
        btnLogout = findViewById(R.id.loginButton);

        btnPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PersonalInfoActivity.class);
                startActivity(intent);
            }
        });

        btnSetReminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RemindersActivity.class);
                startActivity(intent);
            }
        });

        btnNutritionalReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ProfileActivity.this, NutritionalReportActivity.class);
//                startActivity(intent);
            }
        });

        btnAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ProfileActivity.this, AddDeviceActivity.class);
//                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateProfileUI(String name, String age, String location) {
        profile_name.setText(name);
        profile_age.setText("Age: " + age);
        profile_location.setText("Location: " + location);
    }
    public void Nutrition(View view) {
        Intent intent = new Intent(ProfileActivity.this, NutritionActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(ProfileActivity.this, AromasActivity.class);
        startActivity(intent);
    }

    public void Profile(View view) {
        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void Home(View view) {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}