package com.devf5r.kalam.Activity;

import static com.devf5r.kalam.Utils.Constant.BANNER_HOME;
import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.devf5r.kalam.BuildConfig;
import com.devf5r.kalam.Config;
import com.devf5r.kalam.Model.FavoriteList;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.PrefManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class QuoteOfTheDayActivity extends AppCompatActivity {

    private DatabaseReference dbCategories , quote;
    TextView quoteOfTheDay, category;
    TextView  tv_save_quote, likeText;
    LinearLayout layout_quote_header;
    String categorytext, quotes;
    int quoteid;
    ImageView imgIcon, iv_save_quote,tv_quotes_watermark;
    Context context;
    LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
    RelativeLayout relativeLayout;
    private int STORAGE_PERMISSION_CODE = 1;
    LikeButton favBtn;
    private int[] images;
    private int imagesIndex = 0;

    PrefManager prf;
    AdNetwork adNetwork;
    RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_of_the_day);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.quote_of_the_day);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.quote_of_the_day);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prf = new PrefManager(this);

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);
        adNetwork.loadApp(prf.getString("VDN"));

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, Config.REWARD_ADS_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TAG", loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("TAG", "Ad was loaded.");
                    }
                });



        quoteOfTheDay = findViewById(R.id.txtQuote);


        ll_copy_quote = findViewById(R.id.ll_copy_quote);
        ll_quote_save = findViewById(R.id.ll_quote_save);
        ll_quote_share = findViewById(R.id.ll_quote_share);
        relativeLayout = findViewById(R.id.llBackground);
        iv_save_quote = findViewById(R.id.iv_save_quote);
        tv_save_quote = findViewById(R.id.tv_save_quote);
        tv_quotes_watermark = findViewById(R.id.tv_quotes_watermark);

        category = findViewById(R.id.txtCategory);
        imgIcon = findViewById(R.id.imgIcon);
        favBtn = findViewById(R.id.favBtn);
        layout_quote_header = findViewById(R.id.layout_quote_header);
        layout_quote_header.setVisibility(View.VISIBLE);

//        Timer timer = new Timer();
//
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                Random random = new Random();
//                final String[] palabras = {"prueba", "electricidad", "sonido", "fruta", "camisa"};
//                final int rand = random.nextInt(palabras.length);
//                quoteOfTheDay.setText(palabras[rand]);
//            }
//        };

//        timer.scheduleAtFixedRate(timerTask, new Date(), 50000);

        quote = FirebaseDatabase.getInstance().getReference("quoteoftheday");
        quote.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        quoteid = ds.child("id").getValue(int.class);
                        quotes = ds.child("desc").getValue(String.class);
                        categorytext = ds.child("category").getValue(String.class);

                        quoteOfTheDay.setText(quotes);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(quoteid) == 1)
            favBtn.setLiked(true);
        else
            favBtn.setLiked(false);

        favBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = quoteid;
                String name = quotes;
                favoriteList.setId(id);
                favoriteList.setName(name);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    favBtn.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                    startSound();

                } else {
                    favBtn.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                    startSound();
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = quoteid;
                String name = quotes;
                favoriteList.setId(id);
                favoriteList.setName(name);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    favBtn.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                    startSound();

                } else {
                    favBtn.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                    startSound();
                }
            }
        });


        tv_quotes_watermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(QuoteOfTheDayActivity.this)
                        .setIcon(R.drawable.logo)
                        .setTitle("إزالة العلامة المائية")
                        .setMessage("شاهد الفيديو الخاص بإزالة العلامة المائية")
                        .setPositiveButton("مشاهدة", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (mRewardedAd != null){
                                    //Activity activityContext = mCtx;
                                    mRewardedAd.show(QuoteOfTheDayActivity.this, new OnUserEarnedRewardListener() {
                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                            // Handle the reward.
                                            Log.d("TAG", "The user earned the reward.");
                                            int rewardAmount = rewardItem.getAmount();
                                            String rewardType = rewardItem.getType();
                                            prf.setBoolean("myAds",true);
                                            showReward();

                                            tv_quotes_watermark.setVisibility(View.GONE);
                                        }
                                    });
                                } else {
                                    mRewardedAd.show(QuoteOfTheDayActivity.this, new OnUserEarnedRewardListener() {
                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                            // Handle the reward.
                                            Log.d("TAG", "The user earned the reward.");
                                            int rewardAmount = rewardItem.getAmount();
                                            String rewardType = rewardItem.getType();

                                            tv_quotes_watermark.setVisibility(View.GONE);
                                            prf.setBoolean("myAds",true);
                                            showReward();
                                        }
                                    });
                                }
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        //Change Random Backgrounds
        relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int numOfImages = 31;
                images = new int[numOfImages];
                images[0] = R.drawable.img1;
                images[1] = R.drawable.img2;
                images[2] = R.drawable.img3;
                images[3] = R.drawable.img4;
                images[4] = R.drawable.img5;
                images[5] = R.drawable.img6;
                images[6] = R.drawable.img7;
                images[7] = R.drawable.img8;
                images[8] = R.drawable.img9;
                images[9] = R.drawable.img10;
                images[10] = R.drawable.img11;
                images[11] = R.drawable.img12;
                images[12] = R.drawable.img13;
                images[13] = R.drawable.img14;
                images[14] = R.drawable.img15;
                images[15] = R.drawable.img16;
                images[16] = R.drawable.img17;
                images[17] = R.drawable.img18;
                images[18] = R.drawable.img19;
                images[19] = R.drawable.img20;
                images[20] = R.drawable.img21;
                images[21] = R.drawable.img22;
                images[22] = R.drawable.img23;
                images[23] = R.drawable.img24;
                images[24] = R.drawable.img25;
                images[25] = R.drawable.img26;
                images[26] = R.drawable.img27;
                images[27] = R.drawable.img28;
                images[28] = R.drawable.img29;
                images[29] = R.drawable.img30;
                images[30] = R.drawable.img31;

                relativeLayout.setBackgroundResource(images[imagesIndex]);
                ++imagesIndex;  // update index, so that next time it points to next resource
                if (imagesIndex == images.length - 1)
                    imagesIndex = 0; // if we have reached at last index of array, simply restart from beginning
                allSound();
            }
        });

        //when you press save button
        ll_quote_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(QuoteOfTheDayActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    tv_quotes_watermark.setVisibility(View.VISIBLE);
                    Bitmap bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    relativeLayout.draw(canvas);

                    OutputStream fos;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                        Toast.makeText(QuoteOfTheDayActivity.this, "تم الحفظ", Toast.LENGTH_SHORT).show();
                        tv_save_quote.setText("حفظ");
                        iv_save_quote.setImageResource(R.drawable.ic_menu_check);
                        try {
                            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                            fos.flush();
                            fos.close();


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        tv_quotes_watermark.setVisibility(View.INVISIBLE);
                    } else {

                        FileOutputStream outputStream = null;

                        File sdCard = Environment.getExternalStorageDirectory();

                        File directory = new File(sdCard.getAbsolutePath() + "/Latest Quotes");
                        directory.mkdir();

                        String filename = String.format("%d.jpg", System.currentTimeMillis());

                        File outFile = new File(directory, filename);

                        Toast.makeText(QuoteOfTheDayActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        tv_save_quote.setText("Saved");
                        iv_save_quote.setImageResource(R.drawable.ic_menu_check);



                        try {
                            outputStream = new FileOutputStream(outFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                            outputStream.flush();
                            outputStream.close();

                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(outFile));
                            sendBroadcast(intent);


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        tv_quotes_watermark.setVisibility(View.INVISIBLE);

                    }

                }else{
                    //show permission popup
                    requestStoragePermission();
                }
                startSound();
            }
        });

        //copy button
        ll_copy_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", quotes);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(QuoteOfTheDayActivity.this, "تم النسخ", Toast.LENGTH_SHORT).show();
                startSound();
            }
        });

        //When You Press Share Button
        ll_quote_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup();
                startSound();
            }

            private void popup() {
                PopupMenu popup = new PopupMenu(QuoteOfTheDayActivity.this, ll_quote_share);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.sub_text:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, quotes + "\n https://play.google.com/store/apps/details?id="+getPackageName());
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "بعثرة كلام");
                                startActivity(Intent.createChooser(shareIntent, "مشاركة"));
                                Toast.makeText(QuoteOfTheDayActivity.this, "شارك كـ نص", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.sub_image:
                                tv_quotes_watermark.setVisibility(View.VISIBLE);
                                Bitmap bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                                        Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                relativeLayout.draw(canvas);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                                intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id="+getPackageName());
                                startActivity(Intent.createChooser(intent, "بعثرة كلام"));
                                tv_quotes_watermark.setVisibility(View.INVISIBLE);
                                Toast.makeText(QuoteOfTheDayActivity.this, "شارك كـ صورة", Toast.LENGTH_SHORT).show();

                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.menu_item);

                popup.show();
            }
        });
    }

    public void showReward(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, Config.REWARD_ADS_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TAG", loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("TAG", "Ad was loaded.");
                    }
                });
    }

    //Share image tool
    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "بعثرة كلام" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    //Like , Save , Copy , share - Sound Effect
    private void startSound() {
        MediaPlayer likeSound;
        likeSound = MediaPlayer.create(this,R.raw.water);

        if (prf.getBoolean("SOUND")==true) {
            likeSound.start();
        }else {
            likeSound.stop();
        }

    }

    //Sound Effect
    private void allSound() {
        MediaPlayer likeSound;
        likeSound = MediaPlayer.create(this,R.raw.all);

        if (prf.getBoolean("SOUND")==true) {
            likeSound.start();
        }else {
            likeSound.stop();
        }

    }

    private void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)this,Manifest.permission.READ_EXTERNAL_STORAGE)){

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity)QuoteOfTheDayActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        }else {
            ActivityCompat.requestPermissions((Activity)QuoteOfTheDayActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
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
