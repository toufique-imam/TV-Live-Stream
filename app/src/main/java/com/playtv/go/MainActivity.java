package com.playtv.go;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.OneShotPreDrawListener;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.playtv.go.req_functions.TextFileHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String STATE_ITEM = "STATE_ITEM";
    private static final String STATE_ADD = "STATE_ADD";
    public static int OLD_BUTTON = 0;
    public static String FAV_FILE_NAME = "favourites.txt";
    public static TextFileHandler textFileHandler = new TextFileHandler();
    public static HashMap<Pair<String, String>, Boolean> favourite = new HashMap<>();
    public static Integer timeToAdd = 0;
    public static stream_info mxstream = new stream_info();
    DrawerLayout drawer;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    ArrayList<String> categories;
    TextView loading;
    int currentPage = 0;
    boolean addLoaded = false;
    InterstitialAd mInterstitialAd;
    boolean isFavView = false;
    AdRequest adRequest;
    boolean isMaxIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        TextView tv = navigationView.getHeaderView(0).findViewById(R.id.textView_nav_header);
        tv.setText(getIntent().getStringExtra("session"));

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        loading = findViewById(R.id.textView_loading);
        isFavView = getIntent().getBooleanExtra("isFav", false);

        getCategoriesFromFirebase();
        FCMSubscribe();
        textFileHandler.getUserFavourite();
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_ITEM, currentPage);
        savedInstanceState.putInt(STATE_ADD, timeToAdd);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage = savedInstanceState.getInt(STATE_ITEM);
        timeToAdd = savedInstanceState.getInt(STATE_ADD);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        getMenuInflater().inflate(R.menu.search_option_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPage = viewPager.getCurrentItem();
        if (timeToAdd == 1) {
            if (addLoaded) showInterstitial();
            else loadInterstitial();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            final View dialogView = this.getLayoutInflater().inflate(R.layout.dialogue_exit, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dialogView);
            builder.setCancelable(true);
            AlertDialog alertDialog = builder.create();
            MaterialButton accept = dialogView.findViewById(R.id.button_exit_ac);
            MaterialButton reject = dialogView.findViewById(R.id.button_exit_wa);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    MainActivity.super.onBackPressed();
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        if (timeToAdd == 1 || timeToAdd == 2) {
            if (addLoaded) showInterstitial();
            else loadInterstitial();
        }
        timeToAdd = 0;
        super.onResume();
        getCategoriesFromFirebase();
    }

    void FMCTokenSetup() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("Nice", "getInstanceId failed", task.getException());
                return;
            }
            String token = task.getResult().getToken();
            Log.d("Debug ", "Device token: " + token);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DeviceTokens");
            ref.child(token).setValue(false);
        });
    }

    void FCMSubscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic("streaming")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Not Subscribed";
                    }
                    Log.e(LoginActivity.class.getName() + "DEBUG : ", msg);
                    FMCTokenSetup();
                });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    void getCategoriesFromFirebase() {
        if (isNetworkConnected()) {
            categories = new ArrayList<>();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/streaming_now/");
            loading.setVisibility(View.VISIBLE);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    categories.clear();
                    for (DataSnapshot x : dataSnapshot.getChildren()) {
                        categories.add(x.getKey());
                    }
                    viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), categories, isFavView);
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout.setupWithViewPager(viewPager);
                    viewPager.setCurrentItem(currentPage);
                    if (categories.size() == 2)
                        tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    else
                        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

                    loading.setVisibility(View.GONE);
                    toaster("Haga clic en el icono del corazón para agregar / eliminar de favoritos");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    categories.clear();
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    void toaster(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_favourites:
                currentPage = viewPager.getCurrentItem();
                intent = new Intent(this, FavouriteActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_telegram:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/joinchat/POoT_BIePwmEBozSknSi1A")));
                break;
            case R.id.nav_config:
                intent = new Intent(this, ConfigActivity.class);
                intent.putExtra("session", getIntent().getStringExtra("session"));
                startActivity(intent);
                break;
            case R.id.nav_share:
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"playtvgoveneno@gmail.com"});
                Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                Email.putExtra(Intent.EXTRA_TEXT, "Hello Developer Of " + getResources().getString(R.string.app_name) + ", ");
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
                break;
            case R.id.nav_about:
                intent = new Intent(this, PolicyActivity.class);
                startActivity(intent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadInterstitial() {
        mInterstitialAd.loadAd(adRequest);
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                addLoaded = true;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("ADD DEBUG", errorCode + "");
                addLoaded = false;
                loadInterstitial();
                if (isMaxIntent) {
                    setMaxIntent();
                }
            }

            @Override
            public void onAdClosed() {
                addLoaded = false;
                loadInterstitial();
                if (isMaxIntent) {
                    setMaxIntent();
                }
            }
        });
        return interstitialAd;
    }

    void setMaxIntent() {
        isMaxIntent = false;
        mxplayerIntent(mxstream);
    }

    void mxplayerIntent(stream_info now) {
        String packagename = "com.mxtech.videoplayer.ad";
        String packagename1 = "com.mxtech.videoplayer.pro";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packagename);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (!isIntentSafe) {
            intent.setPackage(packagename1);
            isIntentSafe = activities.size() > 0;
            if (!isIntentSafe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packagename)));
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename)));
            }
        }
        intent.setData(Uri.parse(now.video_url));
        String[] headers = {
                "User-Agent", now.user_agent
        };
        intent.putExtra("headers", headers);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            toaster(e.getLocalizedMessage());
        }
    }

    private void showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }
}