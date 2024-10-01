package com.example.nutri;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AromasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAromas;
    private AromaAdapter aromaAdapter;
    private ArrayList<Aroma> aromaList;

    private ArrayList<Aroma> availableAromas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aromas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadAvailableAromas();

        recyclerViewAromas = findViewById(R.id.recyclerViewAromas);
        recyclerViewAromas.setLayoutManager(new LinearLayoutManager(this));

        aromaList = new ArrayList<>();
        aromaAdapter = new AromaAdapter(aromaList);
        recyclerViewAromas.setAdapter(aromaAdapter);

        loadUserAromas();
    }

    private void loadAvailableAromas() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference aromasRef = database.getReference("aromas");

        aromasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                availableAromas.clear();
                for (DataSnapshot aromaSnapshot : dataSnapshot.getChildren()) {
                    String name = aromaSnapshot.child("name").getValue(String.class);
                    String imageUrl = aromaSnapshot.child("imageUrl").getValue(String.class);
                    int likes = aromaSnapshot.child("likes").getValue(Integer.class);
                    int dislikes = aromaSnapshot.child("dislikes").getValue(Integer.class);

                    Aroma aroma = new Aroma(name, likes, dislikes, imageUrl);
                    availableAromas.add(aroma);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AromasActivity.this, "Failed to load available aromas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserAromas() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userAromasRef = database.getReference("user_aromas")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userAromasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                aromaList.clear();
                for (DataSnapshot aromaSnapshot : dataSnapshot.getChildren()) {
                    String name = aromaSnapshot.child("name").getValue(String.class);
                    String imageUrl = aromaSnapshot.child("imageUrl").getValue(String.class);
                    int Likes = aromaSnapshot.child("likes").getValue(Integer.class);
                    int Dislikes = aromaSnapshot.child("dislikes").getValue(Integer.class);
                    Aroma aroma = new Aroma(name,Likes,Dislikes,imageUrl);
                    aromaList.add(aroma);
                }
                aromaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AromasActivity.this, "Failed to load user aromas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Add(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Aroma");

        String[] aromaNames = availableAromas.stream().map(Aroma::getName).toArray(String[]::new);

        builder.setItems(aromaNames, (dialog, which) -> {
            Aroma selectedAroma = availableAromas.get(which);
            addAromaForUser(selectedAroma);
        });

        builder.show();
    }


    private void addAromaForUser(Aroma selectedAroma) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userAromasRef = database.getReference("user_aromas")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(selectedAroma.getName());

        userAromasRef.setValue(selectedAroma).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AromasActivity.this, "Aroma added successfully!", Toast.LENGTH_SHORT).show();
                loadUserAromas();
                aromaAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AromasActivity.this, "Failed to add aroma.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void Nutrition(View view) {
        Intent intent = new Intent(AromasActivity.this, NutritionActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(AromasActivity.this, AromasActivity.class);
        startActivity(intent);
    }

    public void Profile(View view) {
        Intent intent = new Intent(AromasActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void Home(View view) {
        Intent intent = new Intent(AromasActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}
