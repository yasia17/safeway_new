package com.example.newas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewNameAdapter extends RecyclerView.Adapter<ViewNameAdapter.ViewHolder>{

    private ArrayList<Name> names = new ArrayList<>();
    private Context context;
    public ViewNameAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.name_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtName.setText(names.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public void setNames(ArrayList<Name> names) {
        this.names = names;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtName;


        public ViewHolder(View itemView){
            super(itemView);
            txtName = itemView.findViewById(R.id.nameTxt);
        }
    }
}

