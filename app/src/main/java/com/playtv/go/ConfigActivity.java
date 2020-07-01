package com.playtv.go;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.playtv.go.req_functions.TextFileHandler;

public class ConfigActivity extends AppCompatActivity {
    MaterialButton backButton, sign_out_button;
    TextView sessionName;
    ImageButton paypal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        init();
        String text = getResources().getString(R.string.app_name) + "\n" + getIntent().getStringExtra("session");
        sessionName.setText(text);
        backButton.setOnClickListener(v -> {
            super.onBackPressed();
        });
        sign_out_button.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TextFileHandler textFileHandler = new TextFileHandler();
            textFileHandler.updateSessionName("");
            startActivity(i);
            finish();
        });
        paypal.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/playtvgo"));
            startActivity(intent);
        });

    }

    void init() {
        paypal = findViewById(R.id.image_button_paypal);
        sessionName = findViewById(R.id.text_view_config_session_name);
        backButton = findViewById(R.id.button_back_config);
        sign_out_button = findViewById(R.id.button_config_sign_off);
    }
}