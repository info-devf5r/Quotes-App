package com.devf5r.kalam.Activity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.devf5r.kalam.BuildConfig;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.PrefManager;

import java.io.File;
import java.text.DecimalFormat;

public class SettingsActivity extends AppCompatActivity {

    TextView tvCurrentVersion;
    TextView tvSaveLocation;
    TextView tvCacheValue;
    TextView tvNotificationTag;
    TextView tvColumns;
    LinearLayout linearLayoutPolicyPrivacy;
    LinearLayout linearLayoutClearCache;
    LinearLayout linearLayoutColumes;
    Switch switchButtonNotification;
    AlertDialog alertDialog1;
    CharSequence[] values = {" 2 Columns "," 3 Columns "};
    PrefManager prefManager;
    Switch switchDarkMode;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle("Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        prefManager = new PrefManager(this);

        tvCurrentVersion = findViewById(R.id.tvCurrentVersion);
        tvSaveLocation = findViewById(R.id.tvSaveLocation);
        tvCacheValue = findViewById(R.id.tvCacheValue);
        tvNotificationTag = findViewById(R.id.tvNotificationTag);
        tvColumns = findViewById(R.id.tvColumns);
        linearLayoutPolicyPrivacy = findViewById(R.id.linearLayoutPolicyPrivacy);
        linearLayoutClearCache = findViewById(R.id.linearLayoutClearCache);
        linearLayoutColumes = findViewById(R.id.linearLayoutColumes);
        switchButtonNotification = findViewById(R.id.switchButtonNotification);
        switchDarkMode = findViewById(R.id.switch_button_animation);

        tvCurrentVersion.setText(BuildConfig.VERSION_NAME);
        tvSaveLocation.setText(getResources().getString(R.string.storagelocation)+getResources().getString(R.string.app_name));
        tvCacheValue.setText(getResources().getString(R.string.label_cache)+readableFileSize((getDirSize(getCacheDir())) + getDirSize(getExternalCacheDir())));
        tvNotificationTag.setText(getResources().getString(R.string.label_notification)+getResources().getString(R.string.app_name));
        tvColumns.setText(prefManager.getInt("wallpaperColumns")+" Columns");

        linearLayoutClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                clearCache();
            }
        });
        linearLayoutPolicyPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                startActivity(new Intent(SettingsActivity.this, PrivacyActivity.class));
            }
        });
        linearLayoutColumes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                CreateAlertDialog();
            }
        });

        //Night Mode
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public final void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    prefManager.setNightModeState(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    onResume();
                }else {
                    prefManager.setNightModeState(false);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    onResume();
                }
            }
        });

    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

    private void clearCache() {

//        FileUtils.deleteQuietly(getCacheDir());
//        FileUtils.deleteQuietly(getExternalCacheDir());

        new Handler().postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvCacheValue.setText(getResources().getString(R.string.label_cache)+readableFileSize((getDirSize(getCacheDir())) + getDirSize(getExternalCacheDir())));
                Toast.makeText(SettingsActivity.this, getString(R.string.msg_cache_cleared), Toast.LENGTH_SHORT).show();
            }
        }, 3000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void CreateAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Display Wallpaper");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item)
                {
                    case 0:
                        prefManager.setInt("wallpaperColumns",2);
                        prefManager.setString("wallpaperColumnsString","two");
                        onResume();
                        SettingsActivity.super.onBackPressed();
                        startActivity(new Intent(SettingsActivity.this,MainActivity.class));
                        break;
                    case 1:
                        prefManager.setInt("wallpaperColumns",3);
                        prefManager.setString("wallpaperColumnsString","three");
                        onResume();
                        SettingsActivity.super.onBackPressed();
                        startActivity(new Intent(SettingsActivity.this,MainActivity.class));
                        break;
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();
    }

    @SuppressLint("SetTextI18n")
    public void onResume() {
        super.onResume();
        tvColumns.setText(prefManager.getInt("wallpaperColumns")+" Columns");

        if (prefManager.loadNightModeState()==true){
            switchDarkMode.setChecked(true);
        }else {
            switchDarkMode.setChecked(false);

        }
        initCheck();

    }

    private void initCheck() {
        if (prefManager.loadNightModeState()){
            Log.d("Dark", "MODE");
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);   // set status text dark
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}