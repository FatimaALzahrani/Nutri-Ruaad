package com.example.nutri;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RemindersActivity extends AppCompatActivity {

    private EditText etRoutine;
    private LinearLayout btnAddRoutine;
    private ListView lvRoutines;
    private DatabaseReference databaseReference;
    private ArrayList<String> routineList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etRoutine = findViewById(R.id.et_routine);
        btnAddRoutine = findViewById(R.id.btn_add_routine);
        lvRoutines = findViewById(R.id.lv_routines);

        routineList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routineList);
        lvRoutines.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("routines").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        btnAddRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoutine();
            }
        });

        loadRoutines();
    }

    private void addRoutine() {
        String routine = etRoutine.getText().toString().trim();
        if (routine.isEmpty()) {
            Toast.makeText(this, "Please enter a routine", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.push().setValue(routine).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RemindersActivity.this, "Routine added", Toast.LENGTH_SHORT).show();
                etRoutine.setText("");
            } else {
                Toast.makeText(RemindersActivity.this, "Error adding routine", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRoutines() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routineList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String routine = snapshot.getValue(String.class);
                    routineList.add(routine);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RemindersActivity.this, "Error loading routines", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
