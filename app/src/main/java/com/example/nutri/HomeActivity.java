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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    private TextView nextMealText;
    private final String[] mealTimes = {"08:00", "13:00", "18:00"};
    private Timer timer;
    private ProgressBar heartRateProgress, activityLevelProgress, energyProgress;
    private TextView helloText, heartRateText, activityLevelText, energyText;
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
        nextMealText = findViewById(R.id.mealTimeText);
        helloText = findViewById(R.id.helloText);
        energyChart = findViewById(R.id.energyChart);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            DatabaseReference userVitalSignsRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("vital_signs");

            userVitalSignsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer heartRate = snapshot.child("heart_rate").getValue(Integer.class);
                        Integer energyLevel = snapshot.child("energy_level").getValue(Integer.class);
                        Integer oxygenLevel = snapshot.child("oxygen_level").getValue(Integer.class);

                        if (heartRate != null && energyLevel != null && oxygenLevel != null) {
                            heartRateProgress.setProgress(heartRate);
                            energyProgress.setProgress(energyLevel);
                            activityLevelProgress.setProgress(oxygenLevel);

                            heartRateText.setText(heartRate + "%");
                            energyText.setText(energyLevel + "%");
                            activityLevelText.setText(oxygenLevel + "%");
                        } else {
                            Toast.makeText(HomeActivity.this, "Error fetching vital signs", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Failed to read data: " + error.getMessage());
                }
            });

            String userName = user.getDisplayName();
            helloText.setText("Hello, " + (userName != null ? userName : ""));
            setupEnergyChart();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updateNextMealText());
            }
        }, 0, 60000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void updateNextMealText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar now = Calendar.getInstance();

        String currentTime = sdf.format(now.getTime());
        String nextMealTime = getNextMealTime(currentTime);

        if (nextMealTime != null) {
            try {
                long remainingMinutes = getMinutesDifference(currentTime, nextMealTime);

                if (remainingMinutes > 60) {
                    long remainingHours = remainingMinutes / 60;
                    long remainingMins = remainingMinutes % 60;
                    String hourText = remainingHours > 1 ? "hours" : "hour";
                    nextMealText.setText("Your next meal is in " + remainingHours + " " + hourText + (remainingMins > 0 ? " and " + remainingMins + " minutes" : ""));
                } else if (remainingMinutes > 0) {
                    nextMealText.setText("Your next meal is in " + remainingMinutes + " minutes");
                } else {
                    nextMealText.setText("It's time for your next meal!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                nextMealText.setText("Error calculating meal time.");
            }
        } else {
            nextMealText.setText("No more meals today.");
        }
    }

    private String getNextMealTime(String currentTime) {
        for (String mealTime : mealTimes) {
            if (mealTime.compareTo(currentTime) > 0) {
                return mealTime;
            }
        }
        if (currentTime.compareTo(mealTimes[mealTimes.length - 1]) > 0) {
            return mealTimes[mealTimes.length - 1];
        }
        return null;
    }


    private long getMinutesDifference(String startTime, String endTime) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        startCal.setTime(sdf.parse(startTime));
        endCal.setTime(sdf.parse(endTime));

        long diffMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        return diffMillis / (60 * 1000);
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
                .child("energy_levels");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();

                for (int i = 0; i < daysOfWeek.length; i++) {
                    String day = daysOfWeek[i];
                    if (dataSnapshot.hasChild(day)) {
                        Float energyHours = dataSnapshot.child(day).getValue(Float.class);
                        if (energyHours != null) {
                            entries.add(new Entry(i, energyHours));
                        }
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
