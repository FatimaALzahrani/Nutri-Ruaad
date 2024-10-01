package com.example.nutri.Admin;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nutri.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AromaAdapter extends RecyclerView.Adapter<AromaAdapter.AromaViewHolder> {

    private ArrayList<Aroma> aromaList;

    public AromaAdapter(ArrayList<Aroma> aromaList) {
        this.aromaList = aromaList;
    }

    @NonNull
    @Override
    public AromaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aroma, parent, false);
        return new AromaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AromaViewHolder holder, int position) {
        Aroma aroma = aromaList.get(position);
        holder.textViewAromaName.setText(aroma.getName());
        holder.textViewLikes.setText(String.valueOf(aroma.getLikes()));
        holder.textViewDislikes.setText(String.valueOf(aroma.getDislikes()));

        Glide.with(holder.itemView.getContext())
                .load(aroma.getImageUrl())
                .into(holder.imageViewAroma);

        holder.itemView.findViewById(R.id.imageViewLike).setOnClickListener(v -> {
            int currentLikes = (int) aroma.getLikes();
            aroma.setLikes(currentLikes + 1);
            updateAromaInDatabase(aroma, true);
        });

        holder.itemView.findViewById(R.id.imageViewDislike).setOnClickListener(v -> {
            int currentDislikes = (int) aroma.getDislikes();
            aroma.setDislikes(currentDislikes + 1);
            updateAromaInDatabase(aroma, false);
        });
    }

    private void updateAromaInDatabase(Aroma aroma, boolean isLike) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userAromaRef = database.getReference("user_aromas").child(userId).child(aroma.getName());
        DatabaseReference globalAromaRef = database.getReference("aromas").child(aroma.getName());

        userAromaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer userLikes = dataSnapshot.child("likes").getValue(Integer.class);
                Integer userDislikes = dataSnapshot.child("dislikes").getValue(Integer.class);

                if (userLikes == null) userLikes = 0;
                if (userDislikes == null) userDislikes = 0;

                if (isLike) {
                    userLikes++;
                    userAromaRef.child("likes").setValue(userLikes);
                    updateGlobalCounts(globalAromaRef);
                } else {
                    userDislikes++;
                    userAromaRef.child("dislikes").setValue(userDislikes);
                    updateGlobalCounts(globalAromaRef);
                }

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to read data", databaseError.toException());
            }
        });
    }

    private void updateGlobalCounts(DatabaseReference globalAromaRef) {
        globalAromaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long currentLikes = dataSnapshot.child("likes").getValue(Long.class);
                Long currentDislikes = dataSnapshot.child("dislikes").getValue(Long.class);

                if (currentLikes == null) currentLikes = 0L;
                if (currentDislikes == null) currentDislikes = 0L;

                globalAromaRef.child("likes").setValue(++currentLikes);
                globalAromaRef.child("dislikes").setValue(++currentDislikes);

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to update global likes/dislikes", databaseError.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return aromaList.size();
    }

    public static class AromaViewHolder extends RecyclerView.ViewHolder {

        TextView textViewAromaName, textViewLikes, textViewDislikes;
        ImageView imageViewAroma;

        public AromaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAromaName = itemView.findViewById(R.id.textViewAromaName);
            textViewLikes = itemView.findViewById(R.id.textViewLikes);
            textViewDislikes = itemView.findViewById(R.id.textViewDislikes);
            imageViewAroma = itemView.findViewById(R.id.imageViewAroma);
        }
    }
}
