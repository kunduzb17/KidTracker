package com.google.kaist.lavi.java.kidtracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.kaist.lavi.java.kidtracker.KidMessage;
import com.google.kaist.lavi.java.kidtracker.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    ProductGridFragment context;
    ArrayList<KidMessage> kidMessages;

    public MyAdapter(ProductGridFragment c , ArrayList<KidMessage> k)
    {
        context = c;
        kidMessages = k;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shr_product_card,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.location.setText(kidMessages.get(position).getLocation());
        holder.time.setText(kidMessages.get(position).getTime());

//        if(profiles.get(position).getPermission()) {
//            holder.btn.setVisibility(View.VISIBLE);
//            holder.onClick(position);
//        }
    }

    @Override
    public int getItemCount() {
        return kidMessages.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView location, time;
//        ImageView profilePic;
//        Button btn;
        public MyViewHolder(View itemView) {
            super(itemView);
            location = (TextView) itemView.findViewById(R.id.message_location);
            time = (TextView) itemView.findViewById(R.id.message_time);
//            profilePic = (ImageView) itemView.findViewById(R.id.profilePic);
//            btn = (Button) itemView.findViewById(R.id.checkDetails);
        }
//        public void onClick(final int position)
//        {
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }
}