
package com.jian.tangthucac.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.jian.tangthucac.R;
import com.jian.tangthucac.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class UserFragment extends Fragment {

    private TextView tvUserEmail, tvUserId;
    private Button btnLogin, btnLogout;
    private LinearLayout loggedInLayout, notLoggedInLayout;
    private SwitchCompat switchDarkMode;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Initialize UI components
        loggedInLayout = view.findViewById(R.id.loggedInLayout);
        notLoggedInLayout = view.findViewById(R.id.notLoggedInLayout);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserId = view.findViewById(R.id.tvUserId);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // Set up login button
        btnLogin.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginActivity.class)));

        // Set up logout button
        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase Auth
            mAuth.signOut();
            // Update UI after logout
            updateUI(null);
        });

        // Set up dark mode switch
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save dark mode preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // Apply dark mode
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check login status when fragment resumes
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is logged in
            loggedInLayout.setVisibility(View.VISIBLE);
            notLoggedInLayout.setVisibility(View.GONE);

            // Set user email
            tvUserEmail.setText(user.getEmail());

            // Get user ID from Firebase Database
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("id")) {
                        // User already has an ID
                        String id = dataSnapshot.child("id").getValue(String.class);
                        tvUserId.setText("ID: " + id);
                    } else {
                        // Generate a new ID for the user
                        String id = UUID.randomUUID().toString().substring(0, 8);
                        userRef.child("id").setValue(id);
                        tvUserId.setText("ID: " + id);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });

        } else {
            // User is not logged in
            loggedInLayout.setVisibility(View.GONE);
            notLoggedInLayout.setVisibility(View.VISIBLE);
        }
    }
}
