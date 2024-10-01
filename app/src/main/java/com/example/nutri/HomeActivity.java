package com.example.nutri;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ProgressBar heartRateProgress, activityLevelProgress, energyProgress;
    private TextView helloText, nextMealText, heartRateText, activityLevelText, energyText;
    private LineChart energyChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        energyProgress = findViewById(R.id.progressEnergy);
        heartRateProgress = findViewById(R.id.progressHeartRate);
        activityLevelProgress = findViewById(R.id.progressActivityLevel);
        heartRateText = findViewById(R.id.textHeartRate);
        activityLevelText = findViewById(R.id.textActivityLevel);
        energyText = findViewById(R.id.textEnergy);

        energyProgress.setProgress(89);
        heartRateProgress.setProgress(50);
        activityLevelProgress.setProgress(89);

        heartRateText.setText(heartRateProgress.getProgress() + "%");
        energyText.setText(energyProgress.getProgress() + "%");
        activityLevelText.setText(activityLevelProgress.getProgress() + "%");

        helloText = findViewById(R.id.helloText);
        nextMealText = findViewById(R.id.mealTimeText);

        energyChart = findViewById(R.id.energyChart);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userName = user.getDisplayName();
            if (userName != null) {
                helloText.setText("Hello, " + userName);
            } else {
                helloText.setText("Hello");
            }
        } else {
            helloText.setText("Hello");
        }

        nextMealText.setText("Your next meal is in 13 min");
        heartRateProgress.setProgress(50);
        activityLevelProgress.setProgress(89);
        energyProgress.setProgress(89);

        setupEnergyChart();
    }

    private void setupEnergyChart() {
        List<Entry> entries = new ArrayList<>();
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firebase", "User not logged in");
            return;
        }
        String uid = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("energy_levels/week_39");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();

                for (int i = 0; i < daysOfWeek.length; i++) {
                    String day = daysOfWeek[i];
                    if (dataSnapshot.hasChild(day)) {
                        float energyHours = dataSnapshot.child(day).getValue(Float.class);
                        entries.add(new Entry(i, energyHours));
                    }
                }

                LineDataSet dataSet = new LineDataSet(entries, "Energy Level");
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.parseColor("#4DA893"));
                LineData lineData = new LineData(dataSet);
                energyChart.setData(lineData);

                XAxis xAxis = energyChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));

                YAxis leftAxis = energyChart.getAxisLeft();
                leftAxis.setAxisMinimum(0);
                leftAxis.setAxisMaximum(24);

                YAxis rightAxis = energyChart.getAxisRight();
                rightAxis.setEnabled(false);

                energyChart.getDescription().setEnabled(false);
                energyChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database error: " + databaseError.getMessage());
            }
        });
    }


    public void Nutrition(View view) {
        Intent intent = new Intent(HomeActivity.this, NutritionActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(HomeActivity.this, AromasActivity.class);
        startActivity(intent);
    }

    public void Profile(View view) {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void Home(View view) {
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}