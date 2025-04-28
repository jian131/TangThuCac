
package com.jian.tangthucac.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jian.tangthucac.R;
import com.jian.tangthucac.activity.ChatActivity;
import com.jian.tangthucac.activity.SearchActivity;
import com.jian.tangthucac.adapter.BannerAdapter;
import com.jian.tangthucac.adapter.StoryAdapter;
import com.jian.tangthucac.model.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private RecyclerView rvHotStories, rvNewStories;
    private ImageView btnSearch, btnChat;
    private List<Story> allStories = new ArrayList<>();
    private List<Story> hotStories = new ArrayList<>();
    private List<Story> bannerStories = new ArrayList<>();
    private Handler slideHandler = new Handler();
    private int bannerPosition = 0;
    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerViewPager != null) {
                if (bannerPosition >= bannerStories.size()) bannerPosition = 0;
                bannerViewPager.setCurrentItem(bannerPosition++, true);
                slideHandler.postDelayed(this, 3000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        rvHotStories = view.findViewById(R.id.rvHotStories);
        rvNewStories = view.findViewById(R.id.rvNewStories);
        btnSearch = view.findViewById(R.id.btn_search);
        btnChat = view.findViewById(R.id.btn_chat);

        // Set up RecyclerViews
        setupHotStories();
        setupNewStories();

        // Load data from Firebase
        loadStoriesFromFirebase();

        // Set up buttons
        btnSearch.setOnClickListener(v -> startActivity(new Intent(getContext(), SearchActivity.class)));
        btnChat.setOnClickListener(v -> startActivity(new Intent(getContext(), ChatActivity.class)));

        return view;
    }

    private void setupHotStories() {
        rvHotStories.setLayoutManager(new GridLayoutManager(getContext(), 2));
        StoryAdapter hotAdapter = new StoryAdapter(getContext(), hotStories);
        rvHotStories.setAdapter(hotAdapter);
    }

    private void setupNewStories() {
        rvNewStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        StoryAdapter newAdapter = new StoryAdapter(getContext(), allStories);
        rvNewStories.setAdapter(newAdapter);
    }

    private void setupBannerViewPager() {
        BannerAdapter bannerAdapter = new BannerAdapter(getContext(), bannerStories);
        bannerViewPager.setAdapter(bannerAdapter);
        startBannerSlideShow();
    }

    private void startBannerSlideShow() {
        stopBannerSlideShow(); // Stop any previous slideshow
        slideHandler.postDelayed(slideRunnable, 3000);
    }

    private void stopBannerSlideShow() {
        slideHandler.removeCallbacks(slideRunnable);
    }

    private void loadStoriesFromFirebase() {
        DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference("stories");
        storiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allStories.clear();
                hotStories.clear();
                bannerStories.clear();

                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    Story story = storySnapshot.getValue(Story.class);
                    if (story != null) {
                        // Set the id from Firebase
                        story.setId(storySnapshot.getKey());

                        // Add to appropriate lists
                        allStories.add(story);

                        if (story.isHot()) {
                            hotStories.add(story);
                        }

                        // Use first 5 stories for banner
                        if (bannerStories.size() < 5) {
                            bannerStories.add(story);
                        }
                    }
                }

                // Notify adapters
                ((StoryAdapter)rvHotStories.getAdapter()).notifyDataSetChanged();
                ((StoryAdapter)rvNewStories.getAdapter()).notifyDataSetChanged();

                // Set up banner after data is loaded
                setupBannerViewPager();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading stories: " + error.getMessage());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerSlideShow();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerViewPager != null && bannerViewPager.getAdapter() != null &&
            bannerViewPager.getAdapter().getItemCount() > 0) {
            startBannerSlideShow();
        }
    }
}
