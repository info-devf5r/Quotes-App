package com.devf5r.kalam.Activity;

import static com.devf5r.kalam.Utils.Constant.BANNER_HOME;
import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.devf5r.kalam.Adapter.PhotosAdapter;
import com.devf5r.kalam.Model.Quote;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;
import com.devf5r.kalam.Utils.Constant;
import com.devf5r.kalam.Utils.PrefManager;
import com.devf5r.kalam.Utils.RecyclerItemClickListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity {

    List<Quote> wallpaperList;
    List<Quote> favList;
    RecyclerView recyclerView;
    PhotosAdapter adapter;
    Context context;
    DatabaseReference dbWallpapers, dbFavs;
    AppCompatImageView progressBar;
    String category;
    PrefManager prf;
    AdNetwork adNetwork;
    AdsPref adsPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("category");
        setSupportActionBar(toolbar);
        toolbar.setTitle(category);
        setTitle(category);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prf = new PrefManager(this);
        adsPref = new AdsPref(this);
        getData();

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
        adNetwork.loadApp(prf.getString("VDN"));


        favList = new ArrayList<>();
        wallpaperList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotosAdapter(this, wallpaperList);

        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showInterAd(position);
            }
        }));

        progressBar = findViewById(R.id.progressbar);

        dbWallpapers = FirebaseDatabase.getInstance().getReference("photos");


        fetchWallpapers();

    }

    private void getData() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, Constant.URL_TEMP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    final String VDN = response.getString("DN");
                    prf.setString("TEMP",VDN);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showInterAd(int position) {

        final Intent intshow = new Intent(this, All_Inflater_Activity.class);
        intshow.putExtra("POSITION", position);
        intshow.putExtra("array", (Serializable) wallpaperList);
        startActivity(intshow);
        showInterstitialAd();
    }

    private void fetchWallpapers() {
        progressBar.setVisibility(View.VISIBLE);
        dbWallpapers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {

                        Quote quote = wallpaperSnapshot.getValue(Quote.class);


                        favList.add(0,quote);
                    }
                    for (int i = 0; i < favList.size(); i++) {
                        if (favList.get(i).getCategory().equals(category)) {
                            wallpaperList.add(favList.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCheck();
    }

    private void initCheck() {
        if (prf.loadNightModeState()==true){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}