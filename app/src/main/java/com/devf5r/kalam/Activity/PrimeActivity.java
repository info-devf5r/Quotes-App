package com.devf5r.kalam.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.PrefManager;

public class PrimeActivity extends AppCompatActivity {

    TextView tvContinue;
    PrefManager prf;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prf = new PrefManager(this);

        tvContinue = findViewById(R.id.tvContinue);
        tvContinue.setOnClickListener(view -> {
            Toast.makeText(this, "الشراء معطل في التطبيق التجريبي", Toast.LENGTH_SHORT).show();
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // مقبض السهم انقر هنا
        if (item.getItemId() == android.R.id.home) {
            finish(); // أغلق هذا النشاط وارجع إلى معاينة النشاط (إن وجد)
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