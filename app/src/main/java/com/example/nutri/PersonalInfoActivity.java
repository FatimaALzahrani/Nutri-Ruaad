package com.example.nutri;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private EditText etName, etAge, etLocation, etHeight, etWeight, etJob;
    private LinearLayout btnSave;
    TextView text_save;
    private DatabaseReference databaseReference;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etLocation = findViewById(R.id.et_location);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etJob = findViewById(R.id.et_job);
        btnSave = findViewById(R.id.btn_save);
        text_save = findViewById(R.id.text_save);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        loadUserData();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    savePersonalInfo();
                } else {
                    enableEditing();
                }
            }
        });
    }

    private void loadUserData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String age = dataSnapshot.child("age").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String height = dataSnapshot.child("height").getValue(String.class);
                    String weight = dataSnapshot.child("weight").getValue(String.class);
                    String job = dataSnapshot.child("job").getValue(String.class);

                    etName.setText(name);
                    etAge.setText(age);
                    etLocation.setText(location);
                    etHeight.setText(height);
                    etWeight.setText(weight);
                    etJob.setText(job);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PersonalInfoActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableEditing() {
        etName.setEnabled(true);
        etAge.setEnabled(true);
        etLocation.setEnabled(true);
        etHeight.setEnabled(true);
        etWeight.setEnabled(true);
        etJob.setEnabled(true);

        text_save.setText("Save");
        isEditing = true;
    }

    private void savePersonalInfo() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String job = etJob.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || location.isEmpty() || height.isEmpty() || weight.isEmpty() || job.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> personalInfo = new HashMap<>();
        personalInfo.put("name", name);
        personalInfo.put("age", age);
        personalInfo.put("location", location);
        personalInfo.put("height", height);
        personalInfo.put("weight", weight);
        personalInfo.put("job", job);

        databaseReference.updateChildren(personalInfo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PersonalInfoActivity.this, "Info saved", Toast.LENGTH_SHORT).show();
                disableEditing();
            } else {
                Toast.makeText(PersonalInfoActivity.this, "Error saving info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableEditing() {
        etName.setEnabled(false);
        etAge.setEnabled(false);
        etLocation.setEnabled(false);
        etHeight.setEnabled(false);
        etWeight.setEnabled(false);
        etJob.setEnabled(false);

        text_save.setText("Edit");
        isEditing = false;
    }
}
