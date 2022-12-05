package com.devf5r.kalam.Adapter;


import static com.devf5r.kalam.Utils.Constant.INTERSTITIAL_POST_LIST;
import static com.devf5r.kalam.Utils.Constant.ADMOB;
import static com.devf5r.kalam.Utils.Constant.AD_STATUS_ON;
import static com.devf5r.kalam.Utils.Constant.FAN;
import static com.devf5r.kalam.Utils.Constant.STARTAPP;

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
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.devf5r.kalam.BuildConfig;
import com.devf5r.kalam.Config;
import com.devf5r.kalam.Model.FavoriteList;
import com.devf5r.kalam.Activity.MainActivity;
import com.devf5r.kalam.Model.Quote;
import com.devf5r.kalam.R;
import com.devf5r.kalam.Utils.AdNetwork;
import com.devf5r.kalam.Utils.AdsPref;
import com.devf5r.kalam.Utils.Constant;
import com.devf5r.kalam.Utils.PrefManager;
import com.devf5r.kalam.Utils.SharedPref;
import com.devf5r.kalam.Utils.TemplateView;

import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.devf5r.kalam.Utils.NativeTemplateStyle;
import com.facebook.ads.NativeAd;
import com.devf5r.kalam.Utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class WallpapersAdapter extends RecyclerView.Adapter implements ActivityCompat.OnRequestPermissionsResultCallback, Filterable {

    private Context mCtx;
    private List<Quote> wallpaperList;
    private List<Quote> searchList;
    private int STORAGE_PERMISSION_CODE = 1;
    private int[] images;
    private int imagesIndex = 0;
    PrefManager prf;
    private Boolean isAdLoaded = false;
    private final int VIEW_AD = 2;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAdDetails = null;
    AdNetwork adNetwork;
    AdsPref adsPref;
    RewardedAd mRewardedAd;


    public WallpapersAdapter(Context mCtx, List<Quote> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;
        this.searchList = wallpaperList;
        prf = new PrefManager(mCtx);
        adNetwork = new AdNetwork((Activity) mCtx);
        adsPref = new AdsPref(mCtx);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(mCtx, Config.REWARD_ADS_ID,
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;

        if (viewType == VIEW_ITEM) {
            SharedPref sharedPref = new SharedPref(mCtx);

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quotes, parent, false);
            vh = new WallpaperViewHolder(v);

        } else if (viewType == VIEW_AD) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_ads, parent, false);
            vh = new ADViewHolder(v);

        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof WallpaperViewHolder){

            ((WallpaperViewHolder) holder).setIsRecyclable(false);

            final Quote w = searchList.get(position);
            ((WallpaperViewHolder) holder).prf = new PrefManager(mCtx);



            ((WallpaperViewHolder) holder).txtQuote.setText(w.title);

            if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(w.getId()) == 1)
                ((WallpaperViewHolder) holder).favBtn.setLiked(true);
            else
                ((WallpaperViewHolder) holder).favBtn.setLiked(false);

            ((WallpaperViewHolder) holder).favBtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    FavoriteList favoriteList = new FavoriteList();
                    int id = w.getId();
                    String name = w.getTitle();
                    favoriteList.setId(id);
                    favoriteList.setName(name);

                    if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1){
                        ((WallpaperViewHolder) holder).favBtn.setLiked(true);
                        MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                        startSound();

                    } else {
                        ((WallpaperViewHolder) holder).favBtn.setLiked(false);
                        MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                        startSound();
                    }
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    FavoriteList favoriteList = new FavoriteList();
                    int id = w.getId();
                    String name = w.getTitle();
                    favoriteList.setId(id);
                    favoriteList.setName(name);

                    if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1){
                        ((WallpaperViewHolder) holder).favBtn.setLiked(true);
                        MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                        startSound();
                    } else {
                        ((WallpaperViewHolder) holder).favBtn.setLiked(false);
                        MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                        startSound();
                    }
                }
            });

            ((WallpaperViewHolder) holder).tv_quotes_watermark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mCtx)
                            .setIcon(R.drawable.logo_light)
                            .setTitle("إزالة العلامة المائية")
                            .setMessage("شاهد الفيديو الخاص بإزالة العلامة المائية")
                            .setPositiveButton("Watch", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                        if (mRewardedAd != null){
                                            //Activity activityContext = mCtx;
                                            mRewardedAd.show((Activity) mCtx, new OnUserEarnedRewardListener() {
                                                @Override
                                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                    // Handle the reward.
                                                    Log.d("TAG", "The user earned the reward.");
                                                    int rewardAmount = rewardItem.getAmount();
                                                    String rewardType = rewardItem.getType();
                                                    prf.setBoolean("myAds",true);
                                                    showReward();

                                                    ((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.GONE);
                                                }
                                            });
                                        } else {
                                            mRewardedAd.show((Activity) mCtx, new OnUserEarnedRewardListener() {
                                                @Override
                                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                    // Handle the reward.
                                                    Log.d("TAG", "The user earned the reward.");
                                                    int rewardAmount = rewardItem.getAmount();
                                                    String rewardType = rewardItem.getType();

                                                    ((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.GONE);
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
            ((WallpaperViewHolder) holder).relativeLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int numOfImages = 41;
                    images = new int[numOfImages];
                    images[0] = R.drawable.img1;
                    images[1] = R.drawable.img2;
                    images[2] = R.drawable.img3;
                    images[3] = R.drawable.gradient1;
                    images[4] = R.drawable.img4;
                    images[5] = R.drawable.img5;
                    images[6] = R.drawable.img6;
                    images[7] = R.drawable.gradient2;
                    images[8] = R.drawable.img7;
                    images[9] = R.drawable.img8;
                    images[10] = R.drawable.img9;
                    images[11] = R.drawable.gradient3;
                    images[12] = R.drawable.img10;
                    images[13] = R.drawable.img11;
                    images[14] = R.drawable.img12;
                    images[15] = R.drawable.gradient4;
                    images[16] = R.drawable.img13;
                    images[17] = R.drawable.img14;
                    images[18] = R.drawable.img15;
                    images[19] = R.drawable.gradient5;
                    images[20] = R.drawable.img16;
                    images[21] = R.drawable.img17;
                    images[22] = R.drawable.img18;
                    images[23] = R.drawable.gradient6;
                    images[24] = R.drawable.img19;
                    images[25] = R.drawable.img20;
                    images[26] = R.drawable.img21;
                    images[27] = R.drawable.gradient7;
                    images[28] = R.drawable.img22;
                    images[29] = R.drawable.img23;
                    images[30] = R.drawable.img24;
                    images[31] = R.drawable.gradient8;
                    images[32] = R.drawable.img25;
                    images[33] = R.drawable.img26;
                    images[34] = R.drawable.img27;
                    images[35] = R.drawable.gradient9;
                    images[36] = R.drawable.img28;
                    images[37] = R.drawable.img29;
                    images[38] = R.drawable.img30;
                    images[39] = R.drawable.gradient10;
                    images[40] = R.drawable.img31;

                    ((WallpaperViewHolder) holder).relativeLayout.setBackgroundResource(images[imagesIndex]);
                    ++imagesIndex;  // update index, so that next time it points to next resource

                    if (imagesIndex == 4 || imagesIndex == 8 || imagesIndex == 12 || imagesIndex == 16 || imagesIndex == 20
                            || imagesIndex == 24 || imagesIndex == 28 || imagesIndex == 32 || imagesIndex == 36 || imagesIndex == 40){
                        ((WallpaperViewHolder) holder).darkView.setVisibility(View.GONE);
                    } else {
                        ((WallpaperViewHolder) holder).darkView.setVisibility(View.VISIBLE);
                    }

                    if (imagesIndex == images.length - 1)
                        imagesIndex = 0; // if we have reached at last index of array, simply restart from beginning
                    allSound();
                }
            });

            //when you press save button
            ((WallpaperViewHolder) holder).ll_quote_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(mCtx,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.VISIBLE);
                        Bitmap bitmap = Bitmap.createBitmap(((WallpaperViewHolder) holder).relativeLayout.getWidth(), ((WallpaperViewHolder) holder).relativeLayout.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        ((WallpaperViewHolder) holder).relativeLayout.draw(canvas);

                        OutputStream fos;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            ContentResolver resolver = mCtx.getContentResolver();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                            Toast.makeText(mCtx, "File Saved", Toast.LENGTH_SHORT).show();
                            ((WallpaperViewHolder) holder).tv_save_quote.setText("Saved");
                            ((WallpaperViewHolder) holder).iv_save_quote.setImageResource(R.drawable.ic_menu_check);
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
                            //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);
                        } else {

                            FileOutputStream outputStream = null;

                            File sdCard = Environment.getExternalStorageDirectory();

                            File directory = new File(sdCard.getAbsolutePath() + "/Latest Quotes");
                            directory.mkdir();

                            String filename = String.format("%d.jpg", System.currentTimeMillis());

                            File outFile = new File(directory, filename);

                            Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();
                            ((WallpaperViewHolder) holder).tv_save_quote.setText("Saved");
                            ((WallpaperViewHolder) holder).iv_save_quote.setImageResource(R.drawable.ic_menu_check);


                            try {
                                outputStream = new FileOutputStream(outFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                outputStream.flush();
                                outputStream.close();

                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(Uri.fromFile(outFile));
                                mCtx.sendBroadcast(intent);


                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);

                        }

                    } else {
                        //show permission popup
                        requestStoragePermission();
                    }
                    startSound();
                    showInterstitialAd();

                }
            });

            //copy button
            ((WallpaperViewHolder) holder).ll_copy_quote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", w.title);
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mCtx, "Quotes Copied", Toast.LENGTH_SHORT).show();
                    startSound();
                    showInterstitialAd();
                }
            });

            //When You Press Share Button
            ((WallpaperViewHolder) holder).ll_quote_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup();
                    startSound();

                }

                private void popup() {
                    PopupMenu popup = new PopupMenu(mCtx, ((WallpaperViewHolder) holder).ll_quote_share);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.sub_text:
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, w.title + "\n https://play.google.com/store/apps/details?id=" + mCtx.getPackageName());
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Premium Quotes");
                                    mCtx.startActivity(Intent.createChooser(shareIntent, "Share Quote"));
                                    Toast.makeText(mCtx, "Share as Text", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.sub_image:
                                    //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.VISIBLE);
                                    Bitmap bitmap = Bitmap.createBitmap(((WallpaperViewHolder) holder).relativeLayout.getWidth(), ((WallpaperViewHolder) holder).relativeLayout.getHeight(),
                                            Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    ((WallpaperViewHolder) holder).relativeLayout.draw(canvas);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("*/*");
                                    intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + mCtx.getPackageName());
                                    mCtx.startActivity(Intent.createChooser(intent, "Premium Quotes"));
                                    //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);
                                    Toast.makeText(mCtx, "Share as Image", Toast.LENGTH_SHORT).show();
                                    showInterstitialAd();

                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.inflate(R.menu.menu_item);

                    popup.show();
                }
            });

        }else if (holder instanceof ADViewHolder){

            ((ADViewHolder) holder).setIsRecyclable(false);

            final Quote w = searchList.get(position);
            ((ADViewHolder) holder).prf = new PrefManager(mCtx);



            ((ADViewHolder) holder).txtQuote.setText(w.title);

            if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(w.getId()) == 1)
                ((ADViewHolder) holder).favBtn.setLiked(true);
            else
                ((ADViewHolder) holder).favBtn.setLiked(false);

            ((ADViewHolder) holder).favBtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    FavoriteList favoriteList = new FavoriteList();
                    int id = w.getId();
                    String name = w.getTitle();
                    favoriteList.setId(id);
                    favoriteList.setName(name);

                    if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1){
                        ((ADViewHolder) holder).favBtn.setLiked(true);
                        MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                        startSound();

                    } else {
                        ((ADViewHolder) holder).favBtn.setLiked(false);
                        MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                        startSound();
                    }
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    FavoriteList favoriteList = new FavoriteList();
                    int id = w.getId();
                    String name = w.getTitle();
                    favoriteList.setId(id);
                    favoriteList.setName(name);

                    if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1){
                        ((ADViewHolder) holder).favBtn.setLiked(true);
                        MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
                        startSound();
                    } else {
                        ((ADViewHolder) holder).favBtn.setLiked(false);
                        MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                        startSound();
                    }
                }
            });

            ((ADViewHolder) holder).tv_quotes_watermark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mCtx)
                            .setIcon(R.drawable.logo_light)
                            .setTitle("إزالة العلامة المائية")
                            .setMessage("شاهد الفيديو الخاص بإزالة العلامة المائية")
                            .setPositiveButton("Watch", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (mRewardedAd != null) {
                                        //Activity activityContext = mCtx;
                                        mRewardedAd.show((Activity) mCtx, new OnUserEarnedRewardListener() {
                                            @Override
                                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                // Handle the reward.
                                                Log.d("TAG", "The user earned the reward.");
                                                int rewardAmount = rewardItem.getAmount();
                                                String rewardType = rewardItem.getType();
                                                showReward();
                                                ((ADViewHolder) holder).tv_quotes_watermark.setVisibility(View.GONE);
                                            }
                                        });
                                    } else {
                                        mRewardedAd.show((Activity) mCtx, new OnUserEarnedRewardListener() {
                                            @Override
                                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                // Handle the reward.
                                                Log.d("TAG", "The user earned the reward.");
                                                int rewardAmount = rewardItem.getAmount();
                                                String rewardType = rewardItem.getType();
                                                showReward();
                                                ((ADViewHolder) holder).tv_quotes_watermark.setVisibility(View.GONE);
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
            ((ADViewHolder) holder).relativeLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int numOfImages = 41;
                    images = new int[numOfImages];
                    images[0] = R.drawable.img1;
                    images[1] = R.drawable.img2;
                    images[2] = R.drawable.img3;
                    images[3] = R.drawable.gradient1;
                    images[4] = R.drawable.img4;
                    images[5] = R.drawable.img5;
                    images[6] = R.drawable.img6;
                    images[7] = R.drawable.gradient2;
                    images[8] = R.drawable.img7;
                    images[9] = R.drawable.img8;
                    images[10] = R.drawable.img9;
                    images[11] = R.drawable.gradient3;
                    images[12] = R.drawable.img10;
                    images[13] = R.drawable.img11;
                    images[14] = R.drawable.img12;
                    images[15] = R.drawable.gradient4;
                    images[16] = R.drawable.img13;
                    images[17] = R.drawable.img14;
                    images[18] = R.drawable.img15;
                    images[19] = R.drawable.gradient5;
                    images[20] = R.drawable.img16;
                    images[21] = R.drawable.img17;
                    images[22] = R.drawable.img18;
                    images[23] = R.drawable.gradient6;
                    images[24] = R.drawable.img19;
                    images[25] = R.drawable.img20;
                    images[26] = R.drawable.img21;
                    images[27] = R.drawable.gradient7;
                    images[28] = R.drawable.img22;
                    images[29] = R.drawable.img23;
                    images[30] = R.drawable.img24;
                    images[31] = R.drawable.gradient8;
                    images[32] = R.drawable.img25;
                    images[33] = R.drawable.img26;
                    images[34] = R.drawable.img27;
                    images[35] = R.drawable.gradient9;
                    images[36] = R.drawable.img28;
                    images[37] = R.drawable.img29;
                    images[38] = R.drawable.img30;
                    images[39] = R.drawable.gradient10;
                    images[40] = R.drawable.img31;

                    ((ADViewHolder) holder).relativeLayout.setBackgroundResource(images[imagesIndex]);
                    ++imagesIndex;  // update index, so that next time it points to next resource

                    if (imagesIndex == 4 || imagesIndex == 8 || imagesIndex == 12 || imagesIndex == 16 || imagesIndex == 20
                            || imagesIndex == 24 || imagesIndex == 28 || imagesIndex == 32 || imagesIndex == 36 || imagesIndex == 40){
                        ((ADViewHolder) holder).darkView.setVisibility(View.GONE);
                    } else {
                        ((ADViewHolder) holder).darkView.setVisibility(View.VISIBLE);
                    }

                    if (imagesIndex == images.length - 1)
                        imagesIndex = 0; // if we have reached at last index of array, simply restart from beginning
                    allSound();
                }
            });

            //when you press save button
            ((ADViewHolder) holder).ll_quote_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(mCtx,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.VISIBLE);
                        Bitmap bitmap = Bitmap.createBitmap(((ADViewHolder) holder).relativeLayout.getWidth(), ((ADViewHolder) holder).relativeLayout.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        ((ADViewHolder) holder).relativeLayout.draw(canvas);

                        OutputStream fos;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            ContentResolver resolver = mCtx.getContentResolver();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                            Toast.makeText(mCtx, "File Saved", Toast.LENGTH_SHORT).show();
                            ((ADViewHolder) holder).tv_save_quote.setText("Saved");
                            ((ADViewHolder) holder).iv_save_quote.setImageResource(R.drawable.ic_menu_check);
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
                            //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);
                        } else {

                            FileOutputStream outputStream = null;

                            File sdCard = Environment.getExternalStorageDirectory();

                            File directory = new File(sdCard.getAbsolutePath() + "/Latest Quotes");
                            directory.mkdir();

                            String filename = String.format("%d.jpg", System.currentTimeMillis());

                            File outFile = new File(directory, filename);

                            Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();
                            ((ADViewHolder) holder).tv_save_quote.setText("Saved");
                            ((ADViewHolder) holder).iv_save_quote.setImageResource(R.drawable.ic_menu_check);


                            try {
                                outputStream = new FileOutputStream(outFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                outputStream.flush();
                                outputStream.close();

                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(Uri.fromFile(outFile));
                                mCtx.sendBroadcast(intent);


                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);

                        }

                    } else {
                        //show permission popup
                        requestStoragePermission();
                    }
                    startSound();
                    showInterstitialAd();

                }
            });

            //copy button
            ((ADViewHolder) holder).ll_copy_quote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", w.title);
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mCtx, "Quotes Copied", Toast.LENGTH_SHORT).show();
                    startSound();
                    showInterstitialAd();
                }
            });

            //When You Press Share Button
            ((ADViewHolder) holder).ll_quote_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup();
                    startSound();

                }

                private void popup() {
                    PopupMenu popup = new PopupMenu(mCtx, ((ADViewHolder) holder).ll_quote_share);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.sub_text:
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, w.title + "\n https://play.google.com/store/apps/details?id=" + mCtx.getPackageName());
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Premium Quotes");
                                    mCtx.startActivity(Intent.createChooser(shareIntent, "Share Quote"));
                                    Toast.makeText(mCtx, "Share as Text", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.sub_image:
                                    //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.VISIBLE);
                                    Bitmap bitmap = Bitmap.createBitmap(((ADViewHolder) holder).relativeLayout.getWidth(), ((ADViewHolder) holder).relativeLayout.getHeight(),
                                            Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    ((ADViewHolder) holder).relativeLayout.draw(canvas);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("*/*");
                                    intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + mCtx.getPackageName());
                                    mCtx.startActivity(Intent.createChooser(intent, "Premium Quotes"));
                                    //((WallpaperViewHolder) holder).tv_quotes_watermark.setVisibility(View.INVISIBLE);
                                    Toast.makeText(mCtx, "Share as Image", Toast.LENGTH_SHORT).show();
                                    showInterstitialAd();

                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.inflate(R.menu.menu_item);

                    popup.show();
                }
            });

            ((ADViewHolder) holder).bindNativeAd(mCtx);

        }else {

            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    //Like , Save , Copy , share - Sound Effect
    private void startSound() {
        MediaPlayer likeSound;
        likeSound = MediaPlayer.create(mCtx,R.raw.water);

        if (prf.getBoolean("SOUND")==true) {
            likeSound.start();
        }else {
            likeSound.stop();
        }

    }

    //Sound Effect
    private void allSound() {
        MediaPlayer likeSound;
        likeSound = MediaPlayer.create(mCtx,R.raw.all);

        if (prf.getBoolean("SOUND")==true) {
            likeSound.start();
        }else {
            likeSound.stop();
        }

    }

    //Share image tool
    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri bmpUri = null;
        try {
            File file = new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Premium Quotes" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)mCtx,Manifest.permission.READ_EXTERNAL_STORAGE)){

            new AlertDialog.Builder(mCtx)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity)mCtx,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        }else {
            ActivityCompat.requestPermissions((Activity)mCtx,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    //Permisssion for save images
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(mCtx, "Permission ok", Toast.LENGTH_SHORT).show();

            }else

                Toast.makeText(mCtx, "Permission not allow", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }



    class WallpaperViewHolder extends RecyclerView.ViewHolder {


        TextView  tv_save_quote;
        TextView txtQuote , likeText;
        TextView txtCategory;
        ImageView iv_save_quote, tv_quotes_watermark;
        RelativeLayout relativeLayout;
        private Quote qte;
        LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
        ImageView imgIcon;
        LikeButton favBtn;
        PrefManager prf;
        View darkView;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            txtQuote = itemView.findViewById(R.id.txtQuote);
            relativeLayout = itemView.findViewById(R.id.llBackground);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtQuote = itemView.findViewById(R.id.txtQuote);
            tv_quotes_watermark = itemView.findViewById(R.id.tv_quotes_watermark);
            likeText = itemView.findViewById(R.id.tv_like_quote_text);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            ll_copy_quote = itemView.findViewById(R.id.ll_copy_quote);
            ll_quote_save = itemView.findViewById(R.id.ll_quote_save);
            ll_quote_share = itemView.findViewById(R.id.ll_quote_share);
            tv_save_quote = itemView.findViewById(R.id.tv_save_quote);
            iv_save_quote = itemView.findViewById(R.id.iv_save_quote);
            favBtn = itemView.findViewById(R.id.favBtn);
            darkView = itemView.findViewById(R.id.darkView);

            }
        }

    private static class ADViewHolder extends RecyclerView.ViewHolder {

        TextView  tv_save_quote;
        TextView txtQuote , likeText;
        TextView txtCategory;
        ImageView iv_save_quote, tv_quotes_watermark;
        RelativeLayout relativeLayout;
        private Quote qte;
        LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
        ImageView imgIcon;
        LikeButton favBtn;
        PrefManager prf;
        View darkView;

        TemplateView admob_native_ad_container;
        MediaView admob_media_view;
        private NativeAd nativeAd;
        private NativeAdLayout fan_native_ad_container;
        private LinearLayout nativeAdView;
        View startapp_native_ad_container;
        ImageView startapp_native_image;
        TextView startapp_native_title;
        TextView startapp_native_description;
        Button startapp_native_button;

        private ADViewHolder(View view) {
            super(view);

            txtQuote = itemView.findViewById(R.id.txtQuote);
            relativeLayout = itemView.findViewById(R.id.llBackground);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtQuote = itemView.findViewById(R.id.txtQuote);
            tv_quotes_watermark = itemView.findViewById(R.id.tv_quotes_watermark);
            likeText = itemView.findViewById(R.id.tv_like_quote_text);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            ll_copy_quote = itemView.findViewById(R.id.ll_copy_quote);
            ll_quote_save = itemView.findViewById(R.id.ll_quote_save);
            ll_quote_share = itemView.findViewById(R.id.ll_quote_share);
            tv_save_quote = itemView.findViewById(R.id.tv_save_quote);
            iv_save_quote = itemView.findViewById(R.id.iv_save_quote);
            favBtn = itemView.findViewById(R.id.favBtn);
            darkView = itemView.findViewById(R.id.darkView);

            //admob native ad
            admob_native_ad_container = view.findViewById(R.id.admob_native_ad_container);
            admob_media_view = view.findViewById(R.id.media_view);

            //fan native ad
            fan_native_ad_container = view.findViewById(R.id.fan_native_ad_container);

            //startapp native ad
            startapp_native_ad_container = view.findViewById(R.id.startapp_native_ad_container);
            startapp_native_image = view.findViewById(R.id.startapp_native_image);
            startapp_native_title = view.findViewById(R.id.startapp_native_title);
            startapp_native_description = view.findViewById(R.id.startapp_native_description);
            startapp_native_button = view.findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> itemView.performClick());

        }
        private void bindNativeAd(Context context) {

            final SharedPref sharedPref = new SharedPref(context);
            final AdsPref adsPref = new AdsPref(context);

            if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
                switch (adsPref.getAdType()) {
                    case ADMOB:
                        AdLoader adLoader = new AdLoader.Builder(context, adsPref.getAdMobNativeId())
                                .forNativeAd(nativeAd -> {
                                    if (sharedPref.getIsDarkTheme()) {
                                        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundDark));
                                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                                        admob_native_ad_container.setStyles(styles);
                                    } else {
                                        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundLight));
                                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                                        admob_native_ad_container.setStyles(styles);
                                    }
                                    admob_media_view.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                                    admob_native_ad_container.setNativeAd(nativeAd);
                                }).withAdListener(new AdListener() {
                                    @Override
                                    public void onAdLoaded() {
                                        super.onAdLoaded();
                                        admob_native_ad_container.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                        admob_native_ad_container.setVisibility(View.GONE);
                                    }
                                })
                                .build();
                        adLoader.loadAd(Tools.getAdRequest((Activity) context));
                        break;
                    case FAN:
                        if (BuildConfig.DEBUG) {
                            nativeAd = new NativeAd(context, "IMG_16_9_APP_INSTALL#" + adsPref.getFanNativeUnitId());
                        } else {
                            nativeAd = new NativeAd(context, adsPref.getFanNativeUnitId());
                        }
                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(com.facebook.ads.Ad ad) {

                            }

                            @Override
                            public void onError(com.facebook.ads.Ad ad, AdError adError) {

                            }

                            @Override
                            public void onAdLoaded(com.facebook.ads.Ad ad) {
                                // Race condition, load() called again before last ad was displayed
                                fan_native_ad_container.setVisibility(View.VISIBLE);
                                if (nativeAd == null || nativeAd != ad) {
                                    return;
                                }
                                // Inflate Native Ad into Container
                                //inflateAd(nativeAd);
                                nativeAd.unregisterView();
                                // Add the Ad view into the ad container.
                                LayoutInflater inflater = LayoutInflater.from(context);
                                // Inflate the Ad view.  The layout referenced should be the one you created in the last step.


                                nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_medium_template, fan_native_ad_container, false);

                                fan_native_ad_container.addView(nativeAdView);

                                // Add the AdOptionsView
                                LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                                AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, fan_native_ad_container);
                                adChoicesContainer.removeAllViews();
                                adChoicesContainer.addView(adOptionsView, 0);

                                // Create native UI using the ad metadata.
                                TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                                com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                                TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                                TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                                TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                                Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);
                                LinearLayout ad_unit = nativeAdView.findViewById(R.id.fan_unit);

                                // Set the Text.
                                nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                nativeAdBody.setText(nativeAd.getAdBodyText());
                                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                // Create a list of clickable views
                                List<View> clickableViews = new ArrayList<>();
                                clickableViews.add(nativeAdTitle);
                                clickableViews.add(ad_unit);
                                clickableViews.add(nativeAdCallToAction);

                                // Register the Title and CTA button to listen for clicks.
                                nativeAd.registerViewForInteraction(nativeAdView, nativeAdMedia, clickableViews);
                            }

                            @Override
                            public void onAdClicked(com.facebook.ads.Ad ad) {

                            }

                            @Override
                            public void onLoggingImpression(com.facebook.ads.Ad ad) {

                            }
                        };

                        NativeAd.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
                        nativeAd.loadAd(loadAdConfig);
                        break;
                    case STARTAPP:

                        break;
                }
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (wallpaperList.get(position) != null) {
            final AdsPref adsPref = new AdsPref(mCtx);
            int LIMIT_NATIVE_AD = (Constant.MAX_NUMBER_OF_NATIVE_AD_DISPLAYED * adsPref.getNativeAdInterval()) + adsPref.getNativeAdIndex();
            for (int i = adsPref.getNativeAdIndex(); i < LIMIT_NATIVE_AD; i += adsPref.getNativeAdInterval()) {
                if (position == i) {
                    return VIEW_AD;
                }
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.load_more);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    searchList = wallpaperList;
                } else {
                    ArrayList<Quote> filteredList = new ArrayList<>();
                    for (Quote row : wallpaperList) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    searchList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = searchList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                searchList = (ArrayList<Quote>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void showReward(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(mCtx, Config.REWARD_ADS_ID,
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

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }
}
