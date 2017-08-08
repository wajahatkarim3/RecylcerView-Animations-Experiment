package com.wajahatkarim3.recycleranimmanager;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Wajahat on 8/8/2017.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    public String[] categoryLabels = {
            "Literary Fiction",
            "Sci-Fi & Fantasy",
            "Mystery & Suspense",
            "Biography & Memoir",
            "Non-Fiction"
    };

    public String[] colorCodes = {
            "#5e8ef8",
            "#7d6dea",
            "#72589b",
            "#37bfad",
            "#fdaa80"
    };

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new CategoryViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        holder.txtTitle.setText(categoryLabels[position]);
        holder.layoutBackground.setBackgroundColor(Color.parseColor(colorCodes[position]));
    }

    @Override
    public int getItemCount() {
        return categoryLabels.length;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder
    {

        LinearLayout layoutBackground;
        TextView txtTitle;

        public CategoryViewHolder(View itemView) {
            super(itemView);

            layoutBackground = (LinearLayout) itemView.findViewById(R.id.layotuBackground);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
        }
    }
}
