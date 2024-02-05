package com.example.dpatient.ReportsFasting.FastingReports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyViewPagerAdapterFastingReports extends FragmentStateAdapter {
    public MyViewPagerAdapterFastingReports(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case  0:
            default:
                return new FastingDailyReports();
            case 1:
                return new FastingMonthlyReports();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
