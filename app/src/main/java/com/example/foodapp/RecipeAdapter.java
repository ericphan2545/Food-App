package com.example.foodapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void toggleSelection(int position) {
        boolean wasSelected = selectedPositions.contains(position);
        if (wasSelected) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
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

        // Handle selection mode
        boolean isSelected = selectedPositions.contains(position);
        if (holder.checkboxContainer != null) {
            holder.checkboxContainer.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            
            if (isSelectionMode) {
                // Update checkbox appearance with animation
                if (isSelected) {
                    holder.checkboxContainer.setBackgroundResource(R.drawable.bg_checkbox_selected);
                    holder.ivCheckmark.setVisibility(View.VISIBLE);
                    // Animate checkmark appearance
                    holder.ivCheckmark.setAlpha(0f);
                    holder.ivCheckmark.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                    // Scale animation for checkbox
                    holder.checkboxContainer.setScaleX(0.8f);
                    holder.checkboxContainer.setScaleY(0.8f);
                    holder.checkboxContainer.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                } else {
                    holder.checkboxContainer.setBackgroundResource(R.drawable.bg_checkbox_unselected);
                    holder.ivCheckmark.setVisibility(View.GONE);
                    holder.checkboxContainer.setScaleX(1f);
                    holder.checkboxContainer.setScaleY(1f);
                }
            }
        }
        
        // Show overlay when in selection mode but not selected
        if (holder.selectionOverlay != null) {
            if (isSelectionMode && !isSelected) {
                holder.selectionOverlay.setVisibility(View.VISIBLE);
                holder.selectionOverlay.setAlpha(0.2f);
            } else {
                holder.selectionOverlay.setVisibility(View.GONE);
            }
        }
        
        // Add subtle scale effect when in selection mode
        if (isSelectionMode) {
            holder.itemView.setScaleX(isSelected ? 0.98f : 1f);
            holder.itemView.setScaleY(isSelected ? 0.98f : 1f);
        } else {
            holder.itemView.setScaleX(1f);
            holder.itemView.setScaleY(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
            } else {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
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
        FrameLayout checkboxContainer;
        ImageView ivCheckmark;
        View selectionOverlay;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeTime = itemView.findViewById(R.id.tvRecipeTime);
            checkboxContainer = itemView.findViewById(R.id.checkboxContainer);
            ivCheckmark = itemView.findViewById(R.id.ivCheckmark);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }
    }
}
