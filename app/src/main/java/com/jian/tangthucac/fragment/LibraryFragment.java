
package com.jian.tangthucac.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.R;
import com.jian.tangthucac.SharedViewModel;
import com.jian.tangthucac.activity.LoginActivity;
import com.jian.tangthucac.adapter.LibraryAdapter;
import com.jian.tangthucac.model.Library;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvLibrary;
    private TextView tvLoginPrompt;
    private View noStoriesMessage;
    private LibraryAdapter libraryAdapter;
    private List<Library> libraryItems = new ArrayList<>();
    private FirebaseAuth mAuth;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Initialize UI components
        rvLibrary = view.findViewById(R.id.rvLibrary);
        tvLoginPrompt = view.findViewById(R.id.tvLoginPrompt);
        noStoriesMessage = view.findViewById(R.id.noStoriesMessage);

        // Initialize Firebase Auth and ViewModels
        mAuth = FirebaseAuth.getInstance();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up login prompt
        tvLoginPrompt.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginActivity.class)));

        // Observe refresh request from ViewModel
        sharedViewModel.getShouldRefresh().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (Boolean.TRUE.equals(shouldRefresh)) {
                loadLibraryItems();
                sharedViewModel.refreshComplete();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatusAndLoadLibrary();
    }

    private void setupRecyclerView() {
        rvLibrary.setLayoutManager(new GridLayoutManager(getContext(), 2));
        libraryAdapter = new LibraryAdapter(getContext(), libraryItems);
        rvLibrary.setAdapter(libraryAdapter);
    }

    private void checkLoginStatusAndLoadLibrary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in
            tvLoginPrompt.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
            loadLibraryItems();
        } else {
            // User is not logged in
            tvLoginPrompt.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
            noStoriesMessage.setVisibility(View.GONE);
        }
    }

    private void loadLibraryItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference libraryRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId).child("Library");

        libraryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryItems.clear();

                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    Library library = storySnapshot.getValue(Library.class);
                    if (library != null) {
                        libraryItems.add(library);
                    }
                }

                libraryAdapter.notifyDataSetChanged();

                // Show/hide no stories message
                if (libraryItems.isEmpty()) {
                    noStoriesMessage.setVisibility(View.VISIBLE);
                } else {
                    noStoriesMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading library: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải thư viện", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
