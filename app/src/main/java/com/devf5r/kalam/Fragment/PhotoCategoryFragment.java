package com.devf5r.kalam.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.devf5r.kalam.Adapter.PhotoCategoriesAdapter;
import com.devf5r.kalam.Model.Category;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.Anims;

import java.util.ArrayList;
import java.util.List;
// This App is Created by Abbas Developers
public class PhotoCategoryFragment extends Fragment {

    private List<Category> categoryList;
    private AppCompatImageView progressBar;
    private DatabaseReference dbCategories;

    private RecyclerView recyclerView;
    private PhotoCategoriesAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }
    // This App is Created by Abbas Developers
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressbar);
        Anims aVar = new Anims(this.getResources().getDrawable(R.drawable.logo));
        aVar.m14932a(true);
        progressBar.setImageDrawable(aVar);
        progressBar.setVisibility(View.VISIBLE);

        //mAdView = view.findViewById(R.id.adView);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        categoryList = new ArrayList<>();
        adapter = new PhotoCategoriesAdapter(getActivity(), categoryList);
        recyclerView.setAdapter(adapter);


        dbCategories = FirebaseDatabase.getInstance().getReference("imagecategory");
        dbCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String desc = ds.child("desc").getValue(String.class);
                        String thumb = ds.child("thumbnail").getValue(String.class);

                        Category c = new Category(name, desc, thumb);
                        categoryList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}