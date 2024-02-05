package com.example.dpatient.ReportsFasting.AfterEatingReports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyViewPagerAdapterAfterEatingReports extends FragmentStateAdapter {
    public MyViewPagerAdapterAfterEatingReports(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
            default:
                return new AfterEatingDailyReports();
            case 1:
                return new AfterEatingMonthlyReports();
        }

    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
