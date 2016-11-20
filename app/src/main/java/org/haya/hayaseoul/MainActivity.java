package org.haya.hayaseoul;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private ImageView candleImage;
    private ImageView horizontalImage;
    private int currentImageNum = 0;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private boolean bottomAdsTimeExpired = false;

    private static final String MY_TEST_DEVICE_ID = "85F2EB9DB8F9E84968DCFC0F565D5470";
    private static final String MY_FULL_ADS_ID = "ca-app-pub-4730107377137841/3110161618";
    private static final int BOTTOM_ADS_DELAYED_TIME = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        candleImage = (ImageView) findViewById(R.id.candle_image);
        candleImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "candle"));
        horizontalImage = (ImageView) findViewById(R.id.horizontal_image);
        horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image001"));
        horizontalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentImageNum++;
                if (currentImageNum % 6 == 0) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image001"));
                } else if (currentImageNum % 6 == 1) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image002"));
                } else if (currentImageNum % 6 == 2) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image003"));
                } else if (currentImageNum % 6 == 3) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image004"));
                } else if (currentImageNum % 6 == 4) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image005"));
                } else if (currentImageNum % 6 == 5) {
                    horizontalImage.setImageBitmap(getAssetImageBitmap(MainActivity.this, "image006"));
                }
            }
        });

        // 하단광고
        mAdView = (AdView) findViewById(R.id.adView);
        loadBottomAds();

        // 전면광고
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(MY_FULL_ADS_ID);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadFullAds();
                finish();
            }
        });
        loadFullAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bottomAdsTimeExpired = true;
                if (mAdView != null && !isLandscapeMode()) {
                    mAdView.setVisibility(View.VISIBLE);
                }
            }
        }, BOTTOM_ADS_DELAYED_TIME);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            candleImage.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);
            horizontalImage.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            candleImage.setVisibility(View.VISIBLE);
            mAdView.setVisibility(View.VISIBLE);
            horizontalImage.setVisibility(View.GONE);
            if (bottomAdsTimeExpired && mAdView.getVisibility() == View.GONE) {
                mAdView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            super.onBackPressed();
        }
    }

    private void loadFullAds() {
        mInterstitialAd.loadAd(createAdRequest());
    }

    private void loadBottomAds() {
        mAdView.loadAd(createAdRequest());
    }

    private AdRequest createAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(MY_TEST_DEVICE_ID)
                .build();
    }


    private boolean isLandscapeMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private Bitmap getAssetImageBitmap(Context context, String filename) {
        AssetManager assets = context.getResources().getAssets();
        try {
            InputStream buffer = new BufferedInputStream((assets.open(filename + ".png")));
            BitmapFactory.Options options = getBitmapOption(buffer);
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);
            options.inJustDecodeBounds = false;

            InputStream buffer2 = new BufferedInputStream((assets.open(filename + ".png")));
            return BitmapFactory.decodeStream(buffer2, null, options);
        } catch (IOException e) {
            Log.e("log", "exception");
        }
        return null;
    }

    private BitmapFactory.Options getBitmapOption(InputStream buffer) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(buffer, null, options);
        return options;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
