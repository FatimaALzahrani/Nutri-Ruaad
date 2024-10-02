package com.example.nutri.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutri.LoginActivity;
import com.example.nutri.ProfileActivity;
import com.example.nutri.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstronautManageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AstronautAdapter astronautAdapter;
    private List<User> userList;
    private Set<String> adminSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronaut_manage);

        recyclerView = findViewById(R.id.recyclerViewAromas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adminSet = new HashSet<>();

        fetchAdminNames();

        fetchUsersData();
    }

    private void fetchAdminNames() {
        DatabaseReference adminsRef = FirebaseDatabase.getInstance().getReference("admins");
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                    String adminName = adminSnapshot.child("name").getValue(String.class);
                    if (adminName != null) {
                        adminSet.add(adminName);
                    }
                }
                fetchUsersData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchUsersData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String username = userSnapshot.child("name").getValue(String.class);

                    if (username != null && !adminSet.contains(username)) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        String age = userSnapshot.child("age").getValue(String.class);
                        String height = userSnapshot.child("height").getValue(String.class);
                        String job = userSnapshot.child("job").getValue(String.class);
                        String location = userSnapshot.child("location").getValue(String.class);
                        String imageUrl = userSnapshot.child("imageUrl").getValue(String.class);

                        User user = new User(
                                userId,
                                username,
                                email != null ? email : "N/A",
                                age != null ? age : "N/A",
                                height != null ? height : "N/A",
                                job != null ? job : "N/A",
                                location != null ? location : "Unknown",
                                imageUrl != null ? imageUrl : "default_image_url"
                        );
                        userList.add(user);
                    }
                }

                astronautAdapter = new AstronautAdapter(userList, AstronautManageActivity.this);
                recyclerView.setAdapter(astronautAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void Home(View view) {
        Intent intent = new Intent(AstronautManageActivity.this, AdminActivity.class);
        startActivity(intent);
    }

    public void Aromas(View view) {
        Intent intent = new Intent(AstronautManageActivity.this, AdminAromasActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AstronautManageActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
