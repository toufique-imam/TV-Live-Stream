package com.playtv.go;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DynamicFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "category_name";
    private static final String IS_FAVOURITE_VIEW = "isFav";
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    Adapter_channel adapter_channel;
    ArrayList<channelInfo> dataNow;
    String categories;
    SearchManager searchManager;
    SearchView searchView;
    boolean isFavView = false;
    InterstitialAd mInterstitialAd;

    public DynamicFragment() {
    }

    public static DynamicFragment newInstance(String data, boolean isFavView) {
        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_NUMBER, data);
        args.putBoolean(IS_FAVOURITE_VIEW, isFavView);
        fragment.setArguments(args);
        return fragment;
    }

    int getGridSpanCount() {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float xInches = metrics.widthPixels / metrics.xdpi;
        return (int) Math.max(2.0, Math.floor(xInches));
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_option_menu, menu);
        searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Buscar canal");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter_channel.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter_channel.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnClickListener(v -> {
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        dataNow = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_home);
        gridLayoutManager = new GridLayoutManager(getContext(), getGridSpanCount());
        getDataFromFirebase(categories);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = getArguments() != null ? getArguments().getString(ARG_SECTION_NUMBER) : "";
        isFavView = getArguments() != null && getArguments().getBoolean(IS_FAVOURITE_VIEW);
        dataNow = new ArrayList<>();

        setHasOptionsMenu(true);
    }


    void getDataFromFirebase(final String category) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/streaming_now/" + categories);
        dataNow = new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataNow.clear();
                channelInfo tmp;
                for (DataSnapshot x : dataSnapshot.getChildren()) {
                    tmp = new channelInfo();
                    tmp.category = category;
                    tmp.channel_name = x.getKey();
                    if (x.child("poster_path").exists()) {
                        tmp.poster_path = Objects.requireNonNull(x.child("poster_path").getValue()).toString();
                    }
                    tmp.spoken_languages = new ArrayList<>();
                    stream_info streamInfo;
                    for (DataSnapshot y : x.child("spoken_languages").getChildren()) {
                        streamInfo = new stream_info("", "", "");
                        if (y.child("name").exists()) {
                            streamInfo.setName(Objects.requireNonNull(y.child("name").getValue()).toString());
                        }
                        if (y.child("video_url").exists()) {
                            streamInfo.setVideo_url(Objects.requireNonNull(y.child("video_url").getValue()).toString());
                        }
                        if (y.child("user_agent").exists()) {
                            streamInfo.setUser_token(Objects.requireNonNull(y.child("user_agent").getValue()).toString());
                        }
                        tmp.spoken_languages.add(streamInfo);

                    }
                    dataNow.add(tmp);
                }
                adapter_channel = new Adapter_channel(dataNow, getContext(), getActivity(), isFavView);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setAdapter(adapter_channel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dataNow.clear();
            }
        });
    }
}