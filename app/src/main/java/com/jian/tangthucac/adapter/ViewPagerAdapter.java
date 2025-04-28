
package com.jian.tangthucac.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jian.tangthucac.fragment.GenresFragment;
import com.jian.tangthucac.fragment.HomeFragment;
import com.jian.tangthucac.fragment.LibraryFragment;
import com.jian.tangthucac.fragment.UserFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Thêm các Fragment vào danh sách
        fragmentList.add(new HomeFragment());
        fragmentList.add(new LibraryFragment());
        fragmentList.add(new GenresFragment());
        fragmentList.add(new UserFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
