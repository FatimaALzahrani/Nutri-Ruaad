package com.example.nutri.Admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutri.R;
import java.util.List;

public class AstronautAdapter extends RecyclerView.Adapter<AstronautAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;

    public AstronautAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.astronaut_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailsActivity.class);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("userid", user.getUserId());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("age", user.getAge());
            intent.putExtra("height", user.getHeight());
            intent.putExtra("job", user.getJob());
            intent.putExtra("location", user.getLocation());
            intent.putExtra("imageUrl", user.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.Astronaut); // تأكد من مطابقة الـ ID مع الـ XML
        }
    }
}
