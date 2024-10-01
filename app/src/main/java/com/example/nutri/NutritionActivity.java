package com.example.nutri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NutritionActivity extends AppCompatActivity {

    private TextView progressText, goalStatusText;
    private CheckBox radioButtonCheesePies,radioButtonCoffee, radioButtonWater,radioButtonPizza;
    private BarChart barChart;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private DatabaseReference userProgressRef;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nutrition);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressText = findViewById(R.id.progressText);
        goalStatusText = findViewById(R.id.goalStatusText);
        barChart = findViewById(R.id.barChart);
        radioButtonCheesePies = findViewById(R.id.radioButtonCheesePies);
        radioButtonCoffee= findViewById(R.id.radioButtonCoffee);
        radioButtonWater= findViewById(R.id.radioButtonWater);
        radioButtonPizza= findViewById(R.id.radioButtonPizza);
        progressBar= findViewById(R.id.progress);


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userProgressRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("progress");

        databaseReference = FirebaseDatabase.getInstance().getReference("caloriesConsumed");

        fetchChartData();
        loadUserProgress();
    }

    private void loadUserProgress() {
        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean isCoffeeChecked = dataSnapshot.child("Coffee").getValue(Boolean.class);
                Boolean isWaterChecked = dataSnapshot.child("Water").getValue(Boolean.class);
                Boolean isPizzaChecked = dataSnapshot.child("Pizza").getValue(Boolean.class);
                Boolean isCheesePiesChecked = dataSnapshot.child("CheesePies").getValue(Boolean.class);
                Integer savedProgress = dataSnapshot.child("progressCount").getValue(Integer.class);

                if (isCoffeeChecked != null) {
                    radioButtonCoffee.setChecked(isCoffeeChecked);
                }
                if (isWaterChecked != null) {
                    radioButtonWater.setChecked(isWaterChecked);
                }
                if (isPizzaChecked != null) {
                    radioButtonPizza.setChecked(isPizzaChecked);
                }
                if (isCheesePiesChecked != null) {
                    radioButtonCheesePies.setChecked(isCheesePiesChecked);
                }

                if (savedProgress != null) {
                    updateProgress(savedProgress, 4);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Failed to load user progress", databaseError.toException());
            }
        });
    }

    private void fetchChartData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                int index = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Float calories = snapshot.getValue(Float.class);
                        if (calories != null) {
                            entries.add(new BarEntry(index++, calories));
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "Invalid data format: " + e.getMessage());
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Calories Consumed");
                BarData barData = new BarData(dataSet);
                barChart.setData(barData);
                barChart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Failed to read data", databaseError.toException());
            }
        });
    }

    private void updateProgress(int current, int total) {
        progressText.setText(current + "/" + total);
        if (current == total) {
            goalStatusText.setText("Excellent! You've reached your goal.");
        } else if (current >= total / 2) {
            goalStatusText.setText("Great! You are almost at your daily goal.");
        } else {
            goalStatusText.setText("Keep going! You can do it.");
        }
        progressBar.setProgress(current);
    }

    public void Home(View view) {
        Intent intent = new Intent(NutritionActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(NutritionActivity.this, AromasActivity.class);
        startActivity(intent);
    }

    public void Profile(View view) {
        Intent intent = new Intent(NutritionActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void daily(View view) {
        count=0;

        userProgressRef.child("Coffee").setValue(radioButtonCoffee.isChecked());
        userProgressRef.child("Water").setValue(radioButtonWater.isChecked());
        userProgressRef.child("Pizza").setValue(radioButtonPizza.isChecked());
        userProgressRef.child("CheesePies").setValue(radioButtonCheesePies.isChecked());

        if (radioButtonCoffee.isChecked()) {
            count++;
        }
        if (radioButtonWater.isChecked()) {
            count++;
        }
        if (radioButtonPizza.isChecked()) {
            count++;
        }
        if (radioButtonCheesePies.isChecked()) {
            count++;
        }
        userProgressRef.child("progressCount").setValue(count);
        updateProgress(count, 4);
    }
}
