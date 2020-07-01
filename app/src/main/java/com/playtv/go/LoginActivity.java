package com.playtv.go;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.playtv.go.req_functions.TextFileHandler;

import java.util.Objects;

//check user saved info
//if not found ot not matched, open login
public class LoginActivity extends AppCompatActivity {
    public static String SESSION_NAME = "Session";
    public static String SESSION_LOGIN = "Session_login";
    ConstraintLayout linearLayout;
    ConstraintLayout constraintLayout;
    TextInputEditText input_session;
    MaterialButton button_login, button_telegram;
    String sessionNameNow;
    InterstitialAd mInterstitialAd;
    int addloaded = 0;
    AdRequest adRequest;
    TextFileHandler textFileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        textFileHandler = new TextFileHandler();
        // checkLoggedIn();
    }

    void showAdd() {
        //    accepted_policy();
        if (addloaded == 1) {
            showInterstitial();
            //accepted_policy();
        } else {
            accepted_policy();
            //showInterstitial();
        }
    }

    private void checkLoggedIn() {
        if (isNetworkConnected()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Session");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    sessionNameNow = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    if(sessionNameNow==null)sessionNameNow = "";
                    sessionNameNow = sessionNameNow.trim();
                    String str = textFileHandler.getSessionName();
                    if(str==null)str="";
                    str = str.trim();
                    Log.e("SESSION NAME : ", str);
                    //Log.e("SESSION NAME Firebase :" , sessionNameNow);
                    if (sessionNameNow.equals(str)) {
                        Log.e("DEBUG ", "Logged In");
                        showAdd();
                    } else {
                        Log.e("DEBUG ", "Not Logged In");
                        init();
                        setButton_login();
                        setButton_telegram();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    init();
                    getSessionName();
                }
            });
        } else {
            Log.e("DEBUG", "NOT CONNECTED");
            init();
            getSessionName();
        }
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

    private void toaster(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void setButton_telegram() {
        button_telegram.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/joinchat/POoT_BIePwmEBozSknSi1A"));
            startActivity(intent);
        });
    }

    /*
    Save the login info
     */
    void accepted_policy() {
        //SharedPreferences sp = getSharedPreferences(SESSION_NAME, MODE_PRIVATE);
        //SharedPreferences.Editor editor = sp.edit();
        //editor.putString(SESSION_LOGIN, sessionNameNow);
        //editor.apply();
        textFileHandler.updateSessionName(sessionNameNow);
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("session", sessionNameNow);
        startActivity(i);
        finish();
    }

    /*
    Policy Accept dialogue
     */
    void create_dialogue_box() {
        final View dialogView = getLayoutInflater().inflate(R.layout.policy_dialogue, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Privacy And Policy");
        AlertDialog alertDialog = builder.create();
        TextView textView = dialogView.findViewById(R.id.textView_policy_name);
        textView.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(this, PolicyActivity.class);
            startActivity(intent);
        });
        Button accept = dialogView.findViewById(R.id.button_policy_ac);
        Button reject = dialogView.findViewById(R.id.button_policy_wa);
        accept.setOnClickListener(v -> {
            alertDialog.dismiss();
            showAdd();
        });
        reject.setOnClickListener(v -> finish());
        alertDialog.show();
    }

    void setButton_login() {
        button_login.setOnClickListener(v -> {
            if (isNetworkConnected()) {
                String inputString = input_session.getText().toString();
                if (inputString.equals(sessionNameNow)) {
                    textFileHandler.updateSessionName(sessionNameNow);
                    create_dialogue_box();
                } else {
                    input_session.setError("Loading Error : Invalid Session Name");
                    input_session.requestFocus();
                }
            } else {
                toaster("Not connected to Internet");
            }
        });
    }

    void init() {
        linearLayout = findViewById(R.id.linear_layout_login);
        constraintLayout = findViewById(R.id.constraint_layout_splash);
        input_session = findViewById(R.id.edit_text_session);
        button_login = findViewById(R.id.button_login);
        button_telegram = findViewById(R.id.button_telegram);
        crossFade(constraintLayout, linearLayout, 3000);

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
                addloaded = 1;
                checkLoggedIn();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("ADD DEBUG", errorCode + "");
                addloaded = -1;
                checkLoggedIn();
            }

            @Override
            public void onAdClosed() {
                accepted_policy();
            }
        });
        return interstitialAd;
    }

    void getSessionName() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Session");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sessionNameNow = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                setButton_login();
                setButton_telegram();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sessionNameNow = "";
                setButton_login();
                setButton_telegram();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    void crossFade(final View view_from, View view_dest, int animate_duration) {
        view_dest.setAlpha(0f);
        view_dest.setVisibility(View.VISIBLE);
        view_dest.animate()
                .alpha(1f)
                .setDuration(animate_duration)
                .setListener(null);
        view_from.animate()
                .alpha(0f)
                .setDuration(animate_duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view_from.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(LoginActivity.this, "Permiso denegado para leer / escribir su almacenamiento externo.\n" +
                            "Lo cual es necesario para personalizar tu experiencia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}