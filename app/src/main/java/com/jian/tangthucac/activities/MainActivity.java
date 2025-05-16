package com.jian.tangthucac.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.jian.tangthucac.R;
import com.jian.tangthucac.databinding.ActivityMainBinding;
import com.jian.tangthucac.fragments.BookshelfFragment;
import com.jian.tangthucac.fragments.HomeFragment;
import com.jian.tangthucac.fragments.ProfileFragment;
import com.jian.tangthucac.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Thiết lập bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Mặc định hiển thị fragment trang chủ
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Xử lý khi người dùng chọn mục trong bottom navigation
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_search) {
            fragment = new SearchFragment();
        } else if (itemId == R.id.nav_bookshelf) {
            fragment = new BookshelfFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        }

        return loadFragment(fragment);
    }

    /**
     * Phương thức để thay thế fragment hiện tại
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
