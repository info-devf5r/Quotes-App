package com.devf5r.kalam.Activity;



import static com.devf5r.kalam.Utils.Constant.BANNER_HOME;
import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devf5r.kalam.Adapter.FavoriteAdapter;
import com.devf5r.kalam.Model.FavoriteList;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;
import com.devf5r.kalam.Utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
      private RecyclerView rv;
      private FavoriteAdapter adapter;
      RelativeLayout NoQuotes;
      private ArrayList<FavoriteList> imageArry = new ArrayList<FavoriteList>();

      List<FavoriteList> favoriteLists= MainActivity.favoriteDatabase.favoriteDao().getFavoriteData();
      PrefManager prf;
      TextView NoQuotesBtn;
    AdNetwork adNetwork;
    AdsPref adsPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Favorite");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prf = new PrefManager(this);


        NoQuotes = findViewById(R.id.NoQuotes);

        NoQuotesBtn = findViewById(R.id.NoQuotesBtn);
        NoQuotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
            }
        });

        rv=(RecyclerView)findViewById(R.id.rec);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        getFavData();

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
        adNetwork.loadApp(prf.getString("VDN"));
    }

    private void getFavData() {


        for (FavoriteList cn : favoriteLists) {

            imageArry.add(cn);
        }

        if (imageArry.isEmpty()){

            NoQuotes.setVisibility(View.VISIBLE);

        }

        adapter=new FavoriteAdapter(favoriteLists,getApplicationContext());
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
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
