package com.devf5r.kalam.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.onesignal.OneSignal;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.devf5r.kalam.Adapter.TabAdapter;
import com.devf5r.kalam.BuildConfig;
import com.devf5r.kalam.Config;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;
import com.devf5r.kalam.Utils.FavoriteDatabase;
import com.devf5r.kalam.Utils.GDPR;
import com.devf5r.kalam.Utils.PrefManager;

import static com.devf5r.kalam.Utils.Constant.ADMOB;
import static com.devf5r.kalam.Utils.Constant.AD_STATUS_ON;
import static com.devf5r.kalam.Utils.Constant.APPLOVIN;
import static com.devf5r.kalam.Utils.Constant.BANNER_HOME;
import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;
import static com.devf5r.kalam.Utils.Constant.STARTAPP;
import static com.devf5r.kalam.Utils.Constant.UNITY;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    TabLayout tabLayout;
    ViewPager viewPager;
    public static FavoriteDatabase favoriteDatabase;
    private Boolean DialogOpened = false;
    AppCompatButton btnUpgrade;
    private static final String ONESIGNAL_APP_ID = "293007a4-fcf0-4128-97b5-71c764afd10c";
    PrefManager prf;
    AdNetwork adNetwork;
    AdsPref adsPref;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        prf = new PrefManager(this);
        adsPref = new AdsPref(this);

        if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            switch (adsPref.getAdType()) {
                case STARTAPP:
                    StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), true);
                    StartAppAd.disableSplash();
                    break;
                case ADMOB:
                    MobileAds.initialize(this, initializationStatus -> {
                        Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                        for (String adapterClass : statusMap.keySet()) {
                            AdapterStatus status = statusMap.get(adapterClass);
                            assert status != null;
                            Log.d("MyApp", String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status.getDescription(), status.getLatency()));
                            Log.d("Open Bidding", "FAN open bidding with AdMob as mediation partner selected");
                        }
                    });
                    GDPR.updateConsentStatus(this);
                    break;
                case UNITY:
                    UnityAds.addListener(new IUnityAdsListener() {
                        @Override
                        public void onUnityAdsReady(String placementId) {
                            Log.d(TAG, placementId);
                        }

                        @Override
                        public void onUnityAdsStart(String placementId) {

                        }

                        @Override
                        public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {

                        }

                        @Override
                        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String message) {

                        }
                    });
                    UnityAds.initialize(getApplicationContext(), adsPref.getUnityGameId(), BuildConfig.DEBUG, new IUnityAdsInitializationListener() {
                        @Override
                        public void onInitializationComplete() {
                            Log.d(TAG, "Unity Ads Initialization Complete");
                            Log.d(TAG, "Unity Ads Game ID : " + adsPref.getUnityGameId());
                        }

                        @Override
                        public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                            Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                        }
                    });
                    break;
                case APPLOVIN:
                    AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
                    AppLovinSdk.getInstance(this).initializeSdk(config -> {
                    });
                    final String sdkKey = AppLovinSdk.getInstance(getApplicationContext()).getSdkKey();
                    if (!sdkKey.equals(getString(R.string.applovin_sdk_key))) {
                        Log.e(TAG, "AppLovin ERROR : Please update your sdk key in the manifest file.");
                    }
                    Log.d(TAG, "AppLovin SDK Key : " + sdkKey);
                    break;
            }
        }

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
        adNetwork.loadApp(prf.getString("VDN"));

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        favoriteDatabase= Room.databaseBuilder(getApplicationContext(),FavoriteDatabase.class,"myfavdb").allowMainThreadQueries().build();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_action_action);

        //main Fragment
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("خـواطـر"));
        tabLayout.addTab(tabLayout.newTab().setText("صـور"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final TabAdapter adapter = new TabAdapter(this,getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBodyText = "https://play.google.com/store/apps/details?id="+getPackageName();
            intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT,shareBodyText);
            startActivity(Intent.createChooser(intent,"share via"));
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.logo)
                    .setTitle(getString(R.string.app_name))
                    .setMessage("هل أنت متأكد أنك تريد إغلاق هذا التطبيق؟")
                    .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("لا", null)
                    .show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (menuItem.getItemId() == R.id.nav_today){
            startActivity(new Intent(MainActivity.this, QuoteOfTheDayActivity.class));
        }
    //    if (menuItem.getItemId() == R.id.nav_premium){
    //        startActivity(new Intent(MainActivity.this, PrimeActivity.class));
    //    }
        if (menuItem.getItemId() == R.id.nav_favotite){
            startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
        }
        if (menuItem.getItemId() == R.id.nav_rate){
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));
            }catch (ActivityNotFoundException ex){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
            }
        }
        if (menuItem.getItemId() == R.id.nav_share){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBodyText = "https://play.google.com/store/apps/details?id="+getPackageName();
            intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT,shareBodyText);
            startActivity(Intent.createChooser(intent,"share via"));
        }
        if (menuItem.getItemId() == R.id.nav_contact){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.nav_header_subtitle)});
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            i.putExtra(Intent.EXTRA_TEXT   , getResources().getString(R.string.app_name));
            try {
                startActivity(Intent.createChooser(i, "ارسل بريد..."));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "لا يوجد عملاء بريد إلكتروني مثبت.", Toast.LENGTH_SHORT).show();
            }
        }
        if (menuItem.getItemId() == R.id.nav_about){
            showAboutDialog();
        }
        if (menuItem.getItemId() == R.id.nav_settings){

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        if (menuItem.getItemId() == R.id.nav_insta){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.instagram.com/"+Config.INSTAGRAM));
            startActivity(browserIntent);
        }
        if (menuItem.getItemId() == R.id.nav_twitter){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.twitter.com/"+Config.Twitter));
            startActivity(browserIntent);
        }
        if (menuItem.getItemId() == R.id.nav_telegram){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/"+Config.Telegram));
            startActivity(browserIntent);
        }
        if (menuItem.getItemId() == R.id.nav_facebook){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/"+Config.Facebook));
            startActivity(browserIntent);
        }
         if (menuItem.getItemId() == R.id.nav_play_more_apps){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/dev?id="+Config.Play_more_apps));
            startActivity(browserIntent);
        }
         if (menuItem.getItemId() == R.id.nav_whatsapp){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://api.whatsapp.com/send?phone="+Config.Whatsapp));
            startActivity(browserIntent);
        }
        if (menuItem.getItemId() == R.id.nav_night){
            return false;
        }
        return false;
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogCustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.layout_about);

        Button dialog_btn=dialog.findViewById(R.id.btn_done);
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!prf.getString("VDN").equals(Config.DEVELOPER_NAME)){
            finish();
        }
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