package com.devf5r.kalam.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdsPref;
import com.devf5r.kalam.Utils.Anims;
import com.devf5r.kalam.Utils.Constant;
import com.devf5r.kalam.Utils.PrefManager;
import com.devf5r.kalam.Utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import static com.devf5r.kalam.Config.ADMOB_PUB_ID;
import static com.devf5r.kalam.Config.AD_STATUS;
import static com.devf5r.kalam.Config.AD_TYPE;
import static com.devf5r.kalam.Config.APPLOVIN_BANNER_ID;
import static com.devf5r.kalam.Config.APPLOVIN_INTER_ID;
import static com.devf5r.kalam.Config.BANNER_ID;
import static com.devf5r.kalam.Config.DEVELOPER_NAME;
import static com.devf5r.kalam.Config.FACEBOOK_BANNER_ID;
import static com.devf5r.kalam.Config.FACEBOOK_INTER_ID;
import static com.devf5r.kalam.Config.FACEBOOK_NATIVE_ID;
import static com.devf5r.kalam.Config.INTERSTITIAL_ADS_INTERVAL;
import static com.devf5r.kalam.Config.INTER_ID;
import static com.devf5r.kalam.Config.NATIVE_ADS_INTERVAL;
import static com.devf5r.kalam.Config.NATIVE_ADS_POSITION;
import static com.devf5r.kalam.Config.NATIVE_ID;
import static com.devf5r.kalam.Config.STARTAPP_ID;
import static com.devf5r.kalam.Config.UNITY_BANNER_ID;
import static com.devf5r.kalam.Config.UNITY_GAME_ID;
import static com.devf5r.kalam.Config.UNITY_INTER_ID;
import static com.devf5r.kalam.Utils.Constant.APP_NAME;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    TextView developers;
    PrefManager prf;
    RelativeLayout parentLayout;
    AdsPref adsPref;
    SharedPref sharedPref;
    AppCompatImageView logo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prf = new PrefManager(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        developers = findViewById(R.id.developers);
        parentLayout = findViewById(R.id.parentLayout);

        logo = findViewById(R.id.logo);
        Anims aVar = new Anims(this.getResources().getDrawable(R.drawable.logo));
        aVar.m14932a(true);
        logo.setImageDrawable(aVar);
        logo.setVisibility(View.VISIBLE);

        getData();
    }

    private void getData() {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, Constant.URL_DATA, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    final String VDN = response.getString("DN");
                    prf.setString("VDN",VDN);
                    prf.setString("TEMP",APP_NAME);

                    getConfig();

                    Timer myTimer = new Timer();
                    myTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // If you want to modify a view in your Activity
                            SplashActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                                    finish();

                                }
                            });
                        }
                    }, 3000);

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

    private void getConfig() {
        adsPref.saveAds(
                AD_STATUS,
                AD_TYPE,
                ADMOB_PUB_ID,
                ADMOB_PUB_ID,
                BANNER_ID,
                INTER_ID,
                NATIVE_ID,
                DEVELOPER_NAME,
                FACEBOOK_BANNER_ID,
                FACEBOOK_INTER_ID,
                FACEBOOK_NATIVE_ID,
                STARTAPP_ID,
                UNITY_GAME_ID,
                UNITY_BANNER_ID,
                UNITY_INTER_ID,
                APPLOVIN_BANNER_ID,
                APPLOVIN_INTER_ID,
                INTERSTITIAL_ADS_INTERVAL,
                NATIVE_ADS_INTERVAL,
                NATIVE_ADS_POSITION,
                prf.getString("VDN"),
                ""
        );
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
