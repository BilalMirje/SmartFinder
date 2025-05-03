package com.example.aipoweredsmartfinder.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.aipoweredsmartfinder.fragments.ItemsFragment;

/**
 * A simple FragmentStateAdapter that returns a fragment based on the position.
 * - Position 0: Lost items
 * - Position 1: Found items
 */
public class ItemsPagerAdapter extends FragmentStateAdapter {
    
    private static final int NUM_PAGES = 2;
    
    public ItemsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ItemsFragment.newInstance("lost");
            case 1:
                return ItemsFragment.newInstance("found");
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
} 