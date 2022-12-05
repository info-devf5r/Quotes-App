package com.devf5r.kalam.Adapter;

import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devf5r.kalam.Activity.QuotesActivity;
import com.devf5r.kalam.Model.Category;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;

import java.util.List;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private Context mCtx;
    private List<Category> categoryList;
    AdNetwork adNetwork;
    AdsPref adsPref;


    public CategoriesAdapter(Context mCtx, List<Category> categoryList) {
        this.mCtx = mCtx;
        this.categoryList = categoryList;
        adNetwork = new AdNetwork((Activity) mCtx);
        adsPref = new AdsPref(mCtx);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);
        holder.textView.setText(c.name);
        Glide.with(mCtx)
                .load(c.thumb)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        ImageView imageView;
        RelativeLayout relativeLayout;

        public CategoryViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text_view_cat_name);
            imageView = itemView.findViewById(R.id.image_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int p = getAdapterPosition();
            Category c = categoryList.get(p);
            Intent intent = new Intent(mCtx, QuotesActivity.class);
            intent.putExtra("categoryID", c.name);
            mCtx.startActivity(intent);
            showInterstitialAd();

        }
    }
}
