package com.playtv.go;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.playtv.go.MainActivity.timeToAdd;

public class StreamActivity extends AppCompatActivity implements Player.EventListener, PlaybackPreparer {
    private static final String TAG = StreamActivity.class.getName();
    PlayerView playerView;
    SimpleExoPlayer player;
    LinearLayout linearLayout;
    TextView textView_channel_name, textView_cat_name;
    ConstraintLayout constraintLayout;
    ImageButton imageButton;
    String url, name, user_agent, quality;
    boolean inErrorState = false;
    boolean trydefault = false;
    PlayerControlView playerControlView;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private PlaybackStateListener playbackStateListener;


    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();
    }

    void fadeaway(View view, int animate_duration) {
        //view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(animate_duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }
                });
    }

    void fadeIN(View view, int animate_duration) {
        //view.setAlpha(1f);

        view.setVisibility(View.INVISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(animate_duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        fadeaway(linearLayout, animate_duration * 3);
                    }
                });
    }

    void setImageButton() {
        imageButton.setOnClickListener(v -> {
            timeToAdd = 1;
            super.onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        timeToAdd = 1;
        super.onBackPressed();
    }

    void setPlayerView() {
        playerView.setOnTouchListener((v, event) -> {
            //v.performClick();
            linearLayout.setVisibility(View.VISIBLE);
            fadeIN(linearLayout, 1500);
            return false;
        });
    }

    void checkMediaType() {
        if (url.endsWith("mp4")) {
            Toast.makeText(getApplicationContext(), "Can not play this due to security reason", Toast.LENGTH_LONG).show();
            super.onBackPressed();
        }
    }

    void getIntentData() {
        Intent intent = getIntent();
        url = intent.getStringExtra("stream_url");
        user_agent = intent.getStringExtra("user_agent");
        name = intent.getStringExtra("channel_name");
        quality = intent.getStringExtra("quality");
        checkMediaType();
    }

    void init() {
        textView_channel_name = findViewById(R.id.textView_channel_name_exo);
        constraintLayout = findViewById(R.id.constraint_layout_exo);
        linearLayout = findViewById(R.id.linear_layout_exo);
        textView_cat_name = findViewById(R.id.textView_category_exo);
        imageButton = findViewById(R.id.button_back_exo);
        playerView = findViewById(R.id.video_view);
        playerControlView = playerView.findViewById(R.id.exo_controller);

        getIntentData();
        textView_channel_name.setText(name);
        textView_cat_name.setText(quality);

        setImageButton();
        setPlayerView();

        fadeaway(linearLayout, 5000);
        playbackStateListener = new PlaybackStateListener();
    }

    private void initializePlayer() {
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            if (Build.VERSION.SDK_INT > 22)
                player = new SimpleExoPlayer.Builder(this).build();
            else {
                @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;
                DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,
                        null, extensionRendererMode);
                player = new SimpleExoPlayer.Builder(this, renderersFactory).build();
            }
            player.addListener(playbackStateListener);
            player.setPlayWhenReady(playWhenReady);
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);

        }
        playerControlView.setShowVrButton(true);
        playerControlView.setVrButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StreamActivity.this.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    StreamActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    StreamActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        Uri uri = Uri.parse(url);
        MediaSource mediaSource = buildMediaSource(uri, user_agent);
        try {
            player.prepare(mediaSource, false, false);
        } catch (
                Exception e) {
            Log.e("error msg", e.getLocalizedMessage());
        }

        inErrorState = false;
    }

    private MediaSource buildMediaSource(Uri uri, String user_agent) {
        DataSource.Factory factory = new DefaultHttpDataSourceFactory(user_agent);

        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                Log.e(TAG, "MEDIA TYPE DASH");
                return new DashMediaSource.Factory(factory).createMediaSource(uri);
            case C.TYPE_SS:
                Log.e(TAG, "MEDIA TYPE Smooth Streaming");
                return new SsMediaSource.Factory(factory).createMediaSource(uri);
            case C.TYPE_HLS:
                Log.e(TAG, "MEDIA TYPE HLS");
                if (trydefault) {
                    trydefault = false;
                    return new ExtractorMediaSource.Factory(factory)
                            .createMediaSource(uri, null, null);
                } else {
                    return new HlsMediaSource.Factory(factory).createMediaSource(uri);
                }
            case C.TYPE_OTHER:
                Log.e(TAG, "MEDIA TYPE OTHER , Try Progressive");
                if (trydefault) {
                    trydefault = false;
                    return new ExtractorMediaSource.Factory(factory)
                            .createMediaSource(uri, null, null);
                }
                return new ProgressiveMediaSource.Factory(factory).createMediaSource(uri);
            default: {
                return new ExtractorMediaSource.Factory(factory)
                        .createMediaSource(uri, null, null);
            }
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
            initializePlayer();
        } else {
            Log.e("SOMETHING WRONG", "" + error.getSourceException().getMessage());
            initializePlayer();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }

    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT < 24) {
            releasePlayer();
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }

    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            updateResumePosition();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    private void clearResumePosition() {
        currentWindow = C.INDEX_UNSET;
        playbackPosition = C.TIME_UNSET;
    }

    private void updateResumePosition() {
        currentWindow = player.getCurrentWindowIndex();
        playbackPosition = Math.max(0, player.getContentPosition());
    }

    @Override
    public void preparePlayback() {
        initializePlayer();
    }

    private class PlaybackStateListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.e(TAG, "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (inErrorState) {
                // This will only occur if the user has performed a seek whilst in the error state. Update
                // the resume position so that if the user then retries, playback will resume from the
                // position to which they seeked.
                updateResumePosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            Log.e("ERROR", String.valueOf(e.type));
            inErrorState = true;
            trydefault = !trydefault;
            Toast.makeText(getApplicationContext(), "Error ocurrido, presione el botón de reproducción nuevamente para volver a intentar", Toast.LENGTH_LONG).show();
            if (isBehindLiveWindow(e)) {
                clearResumePosition();
                initializePlayer();
            } else {
                updateResumePosition();
            }
        }
    }
}