package com.example.foodapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.foodapp.R;
import com.example.foodapp.models.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
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

        // Handle selection mode
        if (holder.cbSelect != null) {
            holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            holder.cbSelect.setChecked(selectedPositions.contains(position));
            
            holder.cbSelect.setOnCheckedChangeListener(null); // Clear previous listener
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedPositions.size());
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                // Toggle selection when in selection mode
                if (holder.cbSelect != null) {
                    holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
                }
            } else {
                // Normal click behavior
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

    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedPositions.size());
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedPositions);
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(0);
        }
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        ImageView ivFavorite;
        TextView tvRecipeName;
        TextView tvRecipeTime;
        CheckBox cbSelect;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeTime = itemView.findViewById(R.id.tvRecipeTime);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}
