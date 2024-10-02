package com.example.nutri.Admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nutri.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, ageTextView, locationTextView, progressText, smellPreferenceText, appetiteLevelText;
    TextView namebestsmell1,namebestsmell2,namebestsmell3,avgbestsmell1, avgbestsmell2, avgbestsmell3, noSmell;
    private ProgressBar taskCompletionProgressBar;
    private BarChart energyChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        energyChart = findViewById(R.id.energyChart);
        setupChart();
        nameTextView = findViewById(R.id.nameTextView);
        ageTextView = findViewById(R.id.ageTextView);
        locationTextView = findViewById(R.id.locationTextView);
        progressText = findViewById(R.id.progressText);
        namebestsmell1 = findViewById(R.id.namebestsmell1);
        namebestsmell2 = findViewById(R.id.namebestsmell2);
        namebestsmell3 = findViewById(R.id.namebestsmell3);
        avgbestsmell1 = findViewById(R.id.avgbestsmell1);
        avgbestsmell2 = findViewById(R.id.avgbestsmell2);
        avgbestsmell3 = findViewById(R.id.avgbestsmell3);
        noSmell = findViewById(R.id.noSmell);

        appetiteLevelText = findViewById(R.id.totalEnergyText);
        taskCompletionProgressBar = findViewById(R.id.completionProgress);

        String userId = getIntent().getStringExtra("userid");

        fetchUserData(userId);
    }

    private void fetchUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String age = dataSnapshot.child("age").getValue(String.class);
                String location = dataSnapshot.child("location").getValue(String.class);

                nameTextView.setText(name);
                ageTextView.setText(age);
                locationTextView.setText(location);

                calculateTaskCompletion(userRef.child("progress"));

                DatabaseReference aromasRef = FirebaseDatabase.getInstance().getReference("user_aromas").child(userId);
                updateSmellPreferences(aromasRef);

                calculateAppetiteLevel(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void calculateTaskCompletion(DatabaseReference progressRef) {
        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completedTasks = 0;
                int totalTasks = 0;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    boolean isCompleted = taskSnapshot.getValue(Boolean.class);
                    totalTasks++;
                    if (isCompleted) {
                        completedTasks++;
                    }
                }

                int taskCompletionPercentage = (totalTasks > 0) ? (completedTasks * 100 / totalTasks) : 0;
                taskCompletionProgressBar.setProgress(taskCompletionPercentage);
                progressText.setText(taskCompletionPercentage + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateSmellPreferences(DatabaseReference aromasRef) {
        aromasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> progressValues = new ArrayList<>();
                List<String> smellNames = new ArrayList<>();

                for (DataSnapshot smellSnapshot : snapshot.getChildren()) {
                    String smellName = smellSnapshot.child("name").getValue(String.class);
                    Integer likes = smellSnapshot.child("likes").getValue(Integer.class);
                    Integer dislikes = smellSnapshot.child("dislikes").getValue(Integer.class);

                    if (smellName != null && likes != null && dislikes != null) {
                        int totalVotes = likes + dislikes;
                        int percentageLikes = (totalVotes > 0) ? (likes * 100 / totalVotes) : 0;

                        smellNames.add(smellName);
                        progressValues.add(percentageLikes);
                    } else {
                        Log.e("Firebase", "Smell name, likes, or dislikes is null for an entry: " + smellSnapshot.getKey());
                    }
                }

                updateUIWithSmellData(smellNames, progressValues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void updateUIWithSmellData(List<String> smellNames, List<Integer> progressValues) {
        ProgressBar progressHeartRate = findViewById(R.id.progressHeartRate);
        ProgressBar progressActivityLevel = findViewById(R.id.progressActivityLevel);
        ProgressBar progressEnergy = findViewById(R.id.progressEnergy);

        progressHeartRate.setVisibility(View.GONE);
        progressActivityLevel.setVisibility(View.GONE);
        progressEnergy.setVisibility(View.GONE);

        if (progressValues.size() > 0) {
            progressHeartRate.setProgress(progressValues.get(0));
            avgbestsmell1.setText(progressValues.get(0) + "%");
            if (smellNames.size() > 0) {
                namebestsmell1.setText(smellNames.get(0));
            }
            progressHeartRate.setVisibility(View.VISIBLE);
        }

        if (progressValues.size() > 1) {
            progressActivityLevel.setProgress(progressValues.get(1));
            avgbestsmell2.setText(progressValues.get(1) + "%");
            if (smellNames.size() > 1) {
                namebestsmell2.setText(smellNames.get(1));
            }
            progressActivityLevel.setVisibility(View.VISIBLE);
        }

        if (progressValues.size() > 2) {
            progressEnergy.setProgress(progressValues.get(2));
            avgbestsmell3.setText(progressValues.get(2) + "%");
            if (smellNames.size() > 2) {
                namebestsmell3.setText(smellNames.get(2));
            }
            progressEnergy.setVisibility(View.VISIBLE);
        }

        if (progressValues.isEmpty()) {
            Toast.makeText(this, "The Astronaut has not used any aromas yet.", Toast.LENGTH_SHORT).show();
            noSmell.setVisibility(View.VISIBLE);
        }
    }

    private void setupChart() {
        energyChart.getDescription().setEnabled(false);
        energyChart.setTouchEnabled(true);
        energyChart.setDragEnabled(true);
        energyChart.setScaleEnabled(true);

        XAxis xAxis = energyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }

    private void calculateAppetiteLevel(String userId) {
        DatabaseReference vitalSignsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("vital_signs");

        vitalSignsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer energyLevel = snapshot.child("energy_level").getValue(Integer.class);
                    Integer heartRate = snapshot.child("heart_rate").getValue(Integer.class);
                    Integer oxygenLevel = snapshot.child("oxygen_level").getValue(Integer.class);

                    if (energyLevel != null && heartRate != null && oxygenLevel != null) {
                        List<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(1, energyLevel));
                        entries.add(new BarEntry(2, heartRate));
                        entries.add(new BarEntry(3, oxygenLevel));

                        BarDataSet dataSet = new BarDataSet(entries, "Vital Signs");
                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        dataSet.setValueTextSize(12f);

                        BarData barData = new BarData(dataSet);
                        energyChart.setData(barData);

                        XAxis xAxis = energyChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"", "Energy Level", "Heart Rate", "Oxygen Level"}));
                        xAxis.setGranularity(1f);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setDrawGridLines(false);

                        energyChart.invalidate();
                    }
                } else {
                    Log.e("Firebase", "No vital signs data available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    public void Remove(View view) {
        String userId = getIntent().getStringExtra("userid");

        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserDetailsActivity.this, "User data removed successfully", Toast.LENGTH_SHORT).show();

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(UserDetailsActivity.this, "User removed from Firebase Auth", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(UserDetailsActivity.this, "Failed to remove user from Firebase Auth", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(UserDetailsActivity.this, "No user is signed in", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserDetailsActivity.this, "Failed to remove user data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show();
        }
    }
}
