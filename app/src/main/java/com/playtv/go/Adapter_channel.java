package com.playtv.go;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.playtv.go.MainActivity.OLD_BUTTON;
import static com.playtv.go.MainActivity.favourite;
import static com.playtv.go.MainActivity.textFileHandler;
import static com.playtv.go.MainActivity.timeToAdd;

class channelViewHolder extends RecyclerView.ViewHolder {
    MaterialCardView cardView;
    ImageView imageViewfav;
    ImageView imageView;
    TextView textView;

    public channelViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view_channel_card);
        imageView = itemView.findViewById(R.id.imageView_channel);
        textView = itemView.findViewById(R.id.textView_channel_name);
        imageViewfav = itemView.findViewById(R.id.imageView_fav_icon);
    }
}

public class Adapter_channel extends RecyclerView.Adapter<channelViewHolder> implements Filterable {
    ArrayList<channelInfo> data, data_main;
    Context context;
    Activity activity;
    boolean favView;
    stream_info thisisit;
    InterstitialAd mInterstitialAd;
    AdRequest adRequest;
    boolean addLoaded;

    public Adapter_channel(ArrayList<channelInfo> data, Context context, Activity activity, boolean isFav) {
        this.data = new ArrayList<>(data);
        this.data_main = new ArrayList<>(data);
        this.favView = isFav;
        this.context = context;
        this.activity = activity;
        addLoaded = false;
        adRequest = new AdRequest.Builder().build();
    }

    @NonNull
    @Override
    public channelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflat = LayoutInflater.from(context).inflate(R.layout.channel_view, parent, false);
        return new channelViewHolder(inflat);
    }

    String[] getStreamName(int position) {
        int size = data.get(position).spoken_languages.size();
        String[] ans = new String[size];
        for (int i = 0; i < size; i++) {
            ans[i] = data.get(position).spoken_languages.get(i).getName();
        }
        return ans;
    }

    void mxplayerIntent(stream_info now) {
        String packagename = "com.mxtech.videoplayer.ad";
        String packagename1 = "com.mxtech.videoplayer.pro";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packagename);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (!isIntentSafe) {
            intent.setPackage(packagename1);
            isIntentSafe = activities.size() > 0;
            if (!isIntentSafe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packagename)));
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename)));
            }
        }
        intent.setData(Uri.parse(now.video_url));
        String[] headers = {
                "User-Agent", now.user_agent
        };
        intent.putExtra("headers", headers);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            toaster(e.getLocalizedMessage());
        }
    }

    void toaster(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    void createBottomSheetDialogue(final stream_info now, final String channel_name) {
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setContentView(dialogView);
        dialog.show();

        final RadioGroup rg = dialogView.findViewById(R.id.radio_group_sheet);
        if (now.user_agent.isEmpty()) {
            now.user_agent = context.getResources().getString(R.string.app_name) + "/1.0.0";
        }
        Button accept = dialog.findViewById(R.id.button_sheet_accept);
        Button reject = dialog.findViewById(R.id.button_sheet_reject);
        assert accept != null;
        accept.setOnClickListener(v -> {
            int id = rg.getCheckedRadioButtonId();
            RadioButton rb = dialogView.findViewById(id);
            if (rb.getText().toString().equals(activity.getResources().getString(R.string.exo_player_string))) {
                //toaster("EXO SELECTED");
                timeToAdd = 1;
                Intent intent = new Intent(context, StreamActivity.class);
                intent.putExtra("stream_url", now.video_url);
                intent.putExtra("user_agent", now.user_agent);
                intent.putExtra("channel_name", channel_name);
                intent.putExtra("quality", now.name);
                context.startActivity(intent);
                dialog.dismiss();
            } else {
                timeToAdd = 2;
                thisisit = now;
                mInterstitialAd = newInterstitialAd();
                loadInterstitial();
                dialog.dismiss();
            }
        });
        assert reject != null;
        reject.setOnClickListener(v -> dialog.dismiss());
    }

    void createDialogue(@NonNull final channelViewHolder holder, final int position) {
        holder.cardView.setCardBackgroundColor(Color.RED);
        String[] server_list = getStreamName(position);
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_server, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(dialogView);
        builder.setTitle("ELEGIR SERVIDOR");
        builder.setCancelable(true);
        builder.setOnCancelListener(dialog -> {
            holder.cardView.setCardBackgroundColor(Color.WHITE);

        });
        builder.setOnDismissListener(dialog -> {
            OLD_BUTTON += server_list.length;
            holder.cardView.setCardBackgroundColor(Color.WHITE);

        });

        AlertDialog alertDialog = builder.create();

        final int[] chooseposition = {0};
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_server);
        for (int i = 0; i < server_list.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(i);
            radioButton.setText(server_list[i]);
            radioButton.setTextSize(15);
            radioButton.setTextColor(Color.BLACK);

            if (i == 0) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        MaterialButton materialButton = dialogView.findViewById(R.id.button_stream_now);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            chooseposition[0] = checkedId;

        });
        materialButton.setOnClickListener(view -> {
            stream_info now = data.get(position).spoken_languages.get(chooseposition[0]);
            OLD_BUTTON += server_list.length;
            alertDialog.dismiss();
            createBottomSheetDialogue(now, data.get(position).channel_name);
        });

        alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.RED));
        alertDialog.show();
    }

    void loadImage(@NonNull final channelViewHolder holder, int position) {
        Glide.with(activity)
                .load(data.get(position).getPoster_path())
                .placeholder(R.drawable.play_tv_go_logo)
                .into(holder.imageView);
    }

    void removeAt(int position) {

        Log.e("DEBUG", position + "");
        data.remove(position);
        data_main.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size());
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getResources().getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //addLoaded = true;
                showInterstitial();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //addLoaded = false;
                //loadInterstitial();
                mxplayerIntent(thisisit);
            }

            @Override
            public void onAdClosed() {
                //addLoaded = false;
                //loadInterstitial();
                mxplayerIntent(thisisit);
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(context, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final channelViewHolder holder, final int position) {
        holder.cardView.setCardBackgroundColor(Color.WHITE);
        boolean fav;
        if (favourite.containsKey(Pair.create(data.get(position).category, data.get(position).channel_name))) {
            fav = favourite.get(Pair.create(data.get(position).category, data.get(position).channel_name));
        } else {
            fav = false;
        }
        holder.imageViewfav.setImageResource(fav ? R.drawable.ic_baseline_favorite_red_24 : R.drawable.ic_baseline_favorite_gray_24);
        holder.imageViewfav.setContentDescription(fav ? "YES" : "NO");
        holder.imageViewfav.setOnClickListener(v -> {
            if (holder.imageViewfav.getContentDescription().toString().equals("YES")) {
                favourite.put(Pair.create(data.get(position).category, data.get(position).channel_name), false);
                textFileHandler.update();
                data_main.get(position).fav = false;
                data.get(position).fav = false;
                holder.imageViewfav.setImageResource(R.drawable.ic_baseline_favorite_gray_24);
                holder.imageViewfav.setContentDescription("NO");
                if (favView) {
                    removeAt(position);
                }
            } else {
                favourite.put(Pair.create(data.get(position).category, data.get(position).channel_name), true);
                textFileHandler.update();
                data_main.get(position).fav = true;
                data.get(position).fav = true;
                holder.imageViewfav.setImageResource(R.drawable.ic_baseline_favorite_red_24);
                holder.imageViewfav.setContentDescription("YES");
            }
        });
        loadImage(holder, position);
        holder.textView.setText(data.get(position).getChannelName_real());
        holder.cardView.setOnClickListener(v -> createDialogue(holder, position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                ArrayList<channelInfo> xx = new ArrayList<>();
                if (charString.isEmpty()) {
                    xx = data_main;
                } else {
                    channelInfo tmp;
                    for (int i = 0; i < data_main.size(); i++) {
                        tmp = data_main.get(i);
                        if (tmp.contains(charString)) {
                            xx.add(tmp);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = xx;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    data = (ArrayList<channelInfo>) results.values;
                    if (data == null) data = new ArrayList<>();
                    Log.e("DEBUG RES", data.size() + "");
                    notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("DEBUG", Objects.requireNonNull(e.getMessage()));
                }
            }
        };
    }

}
