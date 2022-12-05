package com.devf5r.kalam.Adapter;

import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.devf5r.kalam.Activity.ViewPagerActivity;
import com.devf5r.kalam.Model.Category;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;

import java.util.List;


public class PhotoCategoriesAdapter extends RecyclerView.Adapter<PhotoCategoriesAdapter.CategoryViewHolder> {

    private Context mCtx;
    private List<Category> categoryList;
    AdNetwork adNetwork;
    AdsPref adsPref;

    public PhotoCategoriesAdapter(Context mCtx, List<Category> categoryList) {
        this.mCtx = mCtx;
        this.categoryList = categoryList;
        adNetwork = new AdNetwork((Activity) mCtx);
        adsPref = new AdsPref(mCtx);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_photo_cat, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);
        holder.textView.setText(c.name);
        Glide.with(mCtx)
                .load(c.thumb)
                .into(holder.imageView);

        String[] mColors = {
                "#FFF175",
                "#75EAFF",
                "#B991FF",
                "#8BFF8F",
                "#FF9B94",
                "#E656FF",
                "#7E92FF",
                "#F27DBD",
                "#F07146",
                "#7EAB1D",
                "#4F9A9F",
                "#E707A5",
                "#4E0698",
                "#2E5AE1",
                "#25685D",
                "#5C5DBB",
                "#FE6106",
                "#920363",
                "#ECB10B",
                "#0587D5",
                "#C02642",
                "#B50DF9",
                "#E7A1D5",
                "#673AB7"
        };

//        holder.relativeLayout.setBackgroundColor(Color.parseColor(mColors[position % 24]));
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
            textView = itemView.findViewById(R.id.tv_Category);
            imageView = itemView.findViewById(R.id.iv_Category);
         //   relativeLayout = itemView.findViewById(R.id.colorBackground);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int p = getAdapterPosition();
            Category c = categoryList.get(p);
            Intent intent = new Intent(mCtx, ViewPagerActivity.class);
            intent.putExtra("category", c.name);
            mCtx.startActivity(intent);
            showInterstitialAd();
        }
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

}
