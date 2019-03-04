package com.google.kaist.lavi.java.kidtracker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class ProductCardViewHolder extends RecyclerView.ViewHolder {

    public TextView productTitle;
    public TextView productPrice;

    public ProductCardViewHolder(@NonNull View itemView) {
        super(itemView);
        productTitle = itemView.findViewById(R.id.product_title);
        productPrice = itemView.findViewById(R.id.product_price);

    }
}
