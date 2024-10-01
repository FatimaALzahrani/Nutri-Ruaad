package com.example.nutri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddAromaActivity extends AppCompatActivity {
    private EditText name;
    private ImageView aromaImageView;
    private Uri imageUri;
    private ProgressBar progressBar;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_aroma);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.name);
        aromaImageView = findViewById(R.id.aromaImageView);
        progressBar = findViewById(R.id.progressBar);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button chooseImageButton = findViewById(R.id.chooseImageButton);

        chooseImageButton.setOnClickListener(v -> openFileChooser());

        uploadButton.setOnClickListener(v -> {
            String aromaName = name.getText().toString().trim();
            if (!aromaName.isEmpty() && imageUri != null) {
                addAromaToDatabase(aromaName, imageUri);
            } else {
                Toast.makeText(AddAromaActivity.this, "Please provide a name and select an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Aroma Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            aromaImageView.setImageURI(imageUri);
        }
    }

    private void addAromaToDatabase(String name, Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("aromas_images/" + name);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("aromas");
                    String aromaId = databaseRef.push().getKey();

                    Aroma newAroma = new Aroma(name, 0, 0, imageUrl);
                    databaseRef.child(name).setValue(newAroma);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddAromaActivity.this, "Aroma added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddAromaActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
}
