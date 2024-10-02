package com.example.nutri.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.nutri.Aroma;
import com.example.nutri.R;

import java.util.List;

public class AromaAdapter extends RecyclerView.Adapter<AromaAdapter.AromaViewHolder> {

    private List<Aroma> aromaList;

    public AromaAdapter(List<Aroma> aromaList) {
        this.aromaList = aromaList;
    }

    @NonNull
    @Override
    public AromaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aroma_item_layout, parent, false);
        return new AromaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AromaViewHolder holder, int position) {
        Aroma aroma = aromaList.get(position);
        holder.nameTextView.setText(aroma.getName());
        Glide.with(holder.itemView.getContext())
                .load(aroma.getImageUrl())
                .apply(new RequestOptions())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return aromaList.size();
    }

    public static class AromaViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView imageView;

        public AromaViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.aromaName);
            imageView = itemView.findViewById(R.id.aromaImage);
        }
    }
}
