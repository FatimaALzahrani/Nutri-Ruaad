package com.example.nutri.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.nutri.Aroma;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.example.nutri.R;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private TextView alertMessage;
    private DatabaseReference usersReference;
    private RecyclerView aromaRecyclerView;
    private AromaAdapter aromaAdapter;
    private List<Aroma> aromaList = new ArrayList<>();
    private DatabaseReference aromasRef;
    private BarChart vitalSignsChart;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    alertMessage = findViewById(R.id.alertMessage);
    vitalSignsChart = findViewById(R.id.vitalSignsChart);
    mAuth = FirebaseAuth.getInstance();
    mDatabase = FirebaseDatabase.getInstance().getReference();

    usersReference = FirebaseDatabase.getInstance().getReference("users");

    checkMealProgress();

    loadVitalSignsData();

    }

    private void loadVitalSignsData() {
        DatabaseReference usersRef = mDatabase.child("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> usernames = new ArrayList<>();

                int index = 0;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot vitalSignsSnapshot = userSnapshot.child("vital_signs");

                    if (vitalSignsSnapshot.exists()) {
                        String username = userSnapshot.child("name").getValue(String.class);
                        if (username != null) {
                            usernames.add(username);
                        }

                        Float energyLevel = vitalSignsSnapshot.child("energy_level").getValue(Float.class);
                        Float heartRate = vitalSignsSnapshot.child("heart_rate").getValue(Float.class);
                        Float oxygenLevel = vitalSignsSnapshot.child("oxygen_level").getValue(Float.class);

                        if (energyLevel != null) {
                            entries.add(new BarEntry(index, energyLevel));
                        }
                        if (heartRate != null) {
                            entries.add(new BarEntry(index + 0.2f, heartRate));
                        }
                        if (oxygenLevel != null) {
                            entries.add(new BarEntry(index + 0.4f, oxygenLevel));
                        }
                        index += 1;
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Vital Signs");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                BarData barData = new BarData(dataSet);
                vitalSignsChart.setData(barData);

                XAxis xAxis = vitalSignsChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(usernames));
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setLabelCount(usernames.size());

                vitalSignsChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void checkMealProgress() {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.hasChild("progress")) {
                        DataSnapshot progressSnapshot = userSnapshot.child("progress");

                        if (progressSnapshot.exists()) {
                            boolean allMealsCompleted = true;

                            for (DataSnapshot mealSnapshot : progressSnapshot.getChildren()) {
                                Boolean mealCompleted = mealSnapshot.getValue(Boolean.class);
                                if (mealCompleted != null && !mealCompleted) {
                                    allMealsCompleted = false;
                                    break;
                                }
                            }

                            if (!allMealsCompleted) {
                                String userName = userSnapshot.child("name").getValue(String.class);
                                alertMessage.setText("There is an astronaut who didn’t eat his meals: " + userName);
                                alertMessage.setBackgroundResource(R.drawable.bg_alert);
                                alertMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
                                Log.d("Meals", "User " + userName + " didn’t eat all meals today.");
                                return;
                            }
                        }
                    }
                }
                alertMessage.setText("All astronauts have eaten their meals.");
                alertMessage.setBackgroundResource(R.drawable.rounded_corner_background);
                alertMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_done_all_24, 0, 0, 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving data: " + databaseError.getMessage());
            }
        });

        aromaRecyclerView = findViewById(R.id.aromaRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        aromaRecyclerView.setLayoutManager(gridLayoutManager);

        aromasRef = FirebaseDatabase.getInstance().getReference("aromas");

        aromasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                aromaList.clear();
                for (DataSnapshot aromaSnapshot : snapshot.getChildren()) {
                    Aroma aroma = aromaSnapshot.getValue(Aroma.class);
                    aromaList.add(aroma);
                }
                aromaAdapter = new AromaAdapter(aromaList);
                aromaRecyclerView.setAdapter(aromaAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load aromas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Astronaut(View view) {
        Intent intent = new Intent(AdminActivity.this, AstronautManageActivity.class);
        startActivity(intent);
    }

    public void Home(View view) {
        Intent intent = new Intent(AdminActivity.this, AdminActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(AdminActivity.this, AdminAromasActivity.class);
        startActivity(intent);
    }

}