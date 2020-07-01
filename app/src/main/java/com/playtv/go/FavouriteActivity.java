package com.playtv.go;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.playtv.go.MainActivity.favourite;
import static com.playtv.go.MainActivity.timeToAdd;

public class FavouriteActivity extends AppCompatActivity {
    ArrayList<channelInfo> data;
    RecyclerView recyclerView;
    Adapter_channel adapter_channel;
    GridLayoutManager gridLayoutManager;
    MaterialButton back_button;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        init();
        mInterstitialAd = newInterstitialAd();
        getDataFromFirebase();
    }

    @Override
    protected void onResume() {
        if (timeToAdd == 1) {
            loadInterstitial();
        }
        timeToAdd = 0;
        super.onResume();
    }

    int getGridSpanCount() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float xInches = metrics.widthPixels / metrics.xdpi;
        return (int) Math.max(2.0, Math.floor(xInches));
    }

    void init() {
        recyclerView = findViewById(R.id.recycler_view_fav);
        gridLayoutManager = new GridLayoutManager(this, getGridSpanCount());
        back_button = findViewById(R.id.button_back_fav);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteActivity.super.onBackPressed();
            }
        });
    }

    void getDataFromFirebase() {
        data = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/streaming_now/");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                channelInfo channelInfo;
                data.clear();
                for (DataSnapshot x : dataSnapshot.getChildren()) {
                    for (DataSnapshot y : x.getChildren()) {
                        channelInfo = new channelInfo();
                        channelInfo.category = x.getKey();
                        channelInfo.channel_name = y.getKey();
                        channelInfo.poster_path = y.child("poster_path").getValue().toString();
                        channelInfo.spoken_languages = new ArrayList<>();
                        Pair<String, String> key = Pair.create(channelInfo.category, channelInfo.channel_name);
                        if (favourite.containsKey(key) && favourite.get(key)) {
                            for (DataSnapshot z : y.child("spoken_languages").getChildren()) {
                                stream_info streamInfo = new stream_info();
                                streamInfo.user_agent = z.child("user_agent").getValue().toString();
                                streamInfo.video_url = z.child("video_url").getValue().toString();
                                streamInfo.name = z.child("name").getValue().toString();
                                channelInfo.spoken_languages.add(streamInfo);
                            }
                            data.add(channelInfo);
                        } else {
                            continue;
                        }
                    }
                }
                adapter_channel = new Adapter_channel(data, FavouriteActivity.this, FavouriteActivity.this, true);
                recyclerView.setAdapter(adapter_channel);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                data.clear();
            }
        });
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showInterstitial();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {

            }
        });
        return interstitialAd;
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