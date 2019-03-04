package com.google.kaist.lavi.java.kidtracker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.kaist.lavi.java.kidtracker.network.ImageRequester;
import com.google.kaist.lavi.java.kidtracker.network.ProductEntry;

import java.util.List;

/**
 * Adapter used to show a simple grid of products.
 */
public class ProductCardRecyclerViewAdapter extends RecyclerView.Adapter<ProductCardViewHolder> {

    private List<KidMessage> messageList;

    ProductCardRecyclerViewAdapter(List<KidMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ProductCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shr_product_card, parent, false);
        return new ProductCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductCardViewHolder holder, int position) {
        if (messageList != null && position < messageList.size()) {
            KidMessage message = messageList.get(position);
            holder.productTitle.setText(message.getLocation());
            holder.productPrice.setText(message.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
