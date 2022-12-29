package com.devf5r.msg.Activity;

import static com.devf5r.msg.Utils.Constant.APP_NAME;
import static com.devf5r.msg.Utils.Constant.BANNER_HOME;
import static com.devf5r.msg.Utils.Constant.INTERSTITIAL_POST_LIST;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.devf5r.msg.Adapter.WallpapersAdapter;
import com.devf5r.msg.Config;
import com.devf5r.msg.Model.Quote;
import com.devf5r.msg.R;
import com.devf5r.msg.Utils.AdNetwork;
import com.devf5r.msg.Utils.Anims;
import com.devf5r.msg.Utils.Constant;
import com.devf5r.msg.Utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuotesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    List<Quote> wallpaperList;
    List<Quote> favList;
    RecyclerView recyclerView;
    WallpapersAdapter adapter;
    AppCompatImageView progressBar;
    DatabaseReference dbWallpapers;
    String category;
    PrefManager prf;
    SwipeRefreshLayout swipeRefreshLayout;
    AdNetwork adNetwork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        prf = new PrefManager(this);

        Toast.makeText(this, "Tap to Change Background", Toast.LENGTH_LONG).show();

        Intent intent = getIntent();
        category = intent.getStringExtra("categoryID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("yyy");
        setSupportActionBar(toolbar);
        toolbar.setTitle(category);
        getData();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
        adNetwork.loadApp(prf.getString("VDN"));


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        favList = new ArrayList<>();
        wallpaperList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WallpapersAdapter(this, wallpaperList);
        recyclerView.setAdapter(adapter);




        progressBar = findViewById(R.id.progressbar);
        Anims aVar = new Anims(this.getResources().getDrawable(R.drawable.logo));
        aVar.m14932a(true);
        progressBar.setImageDrawable(aVar);
        dbWallpapers = FirebaseDatabase.getInstance().getReference("ymg");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        fetchWallpapers(category);

    }

    private void fetchWallpapers(final String category) {
        progressBar.setVisibility(View.VISIBLE);
        dbWallpapers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                showRefresh(false);
                Toast.makeText(QuotesActivity.this, "Tap to Change Background", Toast.LENGTH_LONG).show();

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

    private void refreshData() {

        //lyt_no_item.setVisibility(View.GONE);
        wallpaperList.clear();
        adapter.notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchWallpapers(category);
            }
        }, 2000);


    }

    private void showRefresh(boolean show) {
        if (show) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement the filter logic
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    public void onResume() {
        super.onResume();
        initCheck();

        if (!prf.getString("VDN").equals(Config.DEVELOPER_NAME)){
            finish();
        }
        if (!prf.getString("TEMP").equals(APP_NAME)){
            finish();
        }
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
