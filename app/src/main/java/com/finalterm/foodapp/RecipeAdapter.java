package com.finalterm.foodapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.tvRecipeName.setText(recipe.getName());
        holder.tvRecipeTime.setText(recipe.getTime());
        
        // Load image - priority: Base64 > URL > Resource > Placeholder
        if (recipe.getImageBase64() != null && !recipe.getImageBase64().isEmpty()) {
            // Load from Base64 (Firestore local storage)
            try {
                byte[] decodedBytes = Base64.decode(recipe.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    holder.ivRecipeImage.setImageBitmap(bitmap);
                } else {
                    holder.ivRecipeImage.setImageResource(R.drawable.img_placeholder);
                }
            } catch (Exception e) {
                holder.ivRecipeImage.setImageResource(R.drawable.img_placeholder);
            }
        } else if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            // Load from URL using Glide
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_placeholder))
                    .into(holder.ivRecipeImage);
        } else if (recipe.getImageResId() != 0) {
            // Load from resource if URL not available
            holder.ivRecipeImage.setImageResource(recipe.getImageResId());
        } else {
            // Set placeholder
            holder.ivRecipeImage.setImageResource(R.drawable.img_placeholder);
        }

        // Show favorite icon if favorited
        if (holder.ivFavorite != null) {
            holder.ivFavorite.setVisibility(recipe.isFavorite() ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateList(List<Recipe> newList) {
        recipeList.clear();
        recipeList.addAll(newList);
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        ImageView ivFavorite;
        TextView tvRecipeName;
        TextView tvRecipeTime;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeTime = itemView.findViewById(R.id.tvRecipeTime);
        }
    }
}
