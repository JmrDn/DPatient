package com.example.dpatient.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dpatient.ReportsFasting.After2_3HoursEating;
import com.example.dpatient.ReportsFasting.AfterEatingFragment;
import com.example.dpatient.ReportsFasting.FastingFragment;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
            default:
                return new FastingFragment();
            case 1:
                return new AfterEatingFragment();
            case 2:
                return new After2_3HoursEating();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
