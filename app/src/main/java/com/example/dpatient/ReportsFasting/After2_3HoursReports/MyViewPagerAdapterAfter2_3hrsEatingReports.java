package com.example.dpatient.ReportsFasting.After2_3HoursReports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dpatient.ReportsFasting.AfterEatingReports.AfterEatingMonthlyReports;

public class MyViewPagerAdapterAfter2_3hrsEatingReports extends FragmentStateAdapter {
    public MyViewPagerAdapterAfter2_3hrsEatingReports(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
            default:
                return new After2_3HoursDailyReports();
            case 1:
                return new After2_3HoursMonthlyReports();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
