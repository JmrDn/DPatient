package com.example.dpatient.ReportsFasting;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dpatient.R;
import com.example.dpatient.ReportsFasting.AfterEatingReports.MyViewPagerAdapterAfterEatingReports;
import com.example.dpatient.ReportsFasting.FastingReports.MyViewPagerAdapterFastingReports;
import com.example.dpatient.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class AfterEatingFragment extends Fragment {
    private TextView overAllGlucoseAverage;
    private TextView overAllGlucoseHighest;
    private TextView overAllGlucoseLowest;
    private UserDetails userDetails;
    private String patientId;

    private MyViewPagerAdapterAfterEatingReports myViewPagerAdapter;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_after_eating, container, false);
        initWidgets(view);
        setUpAllOverData();

        myViewPagerAdapter = new MyViewPagerAdapterAfterEatingReports(getActivity());
        viewPager2.setAdapter(myViewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
        return view;
    }
    private void setUpAllOverData() {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();

                                if (documentSnapshot.exists()){
                                    if (documentSnapshot.contains("after_eating_average") &&
                                            documentSnapshot.contains("after_eating_highest") &&
                                            documentSnapshot.contains("after_eating_lowest")){

                                        long fastingGlucoseAverage = documentSnapshot.getLong("after_eating_average");
                                        long fastingHighestGlucose = documentSnapshot.getLong("after_eating_highest");
                                        long fastingLowestGlucose = documentSnapshot.getLong("after_eating_lowest");

                                        overAllGlucoseAverage.setText(String.valueOf(fastingGlucoseAverage));
                                        overAllGlucoseHighest.setText(String.valueOf(fastingHighestGlucose));
                                        overAllGlucoseLowest.setText(String.valueOf(fastingLowestGlucose));
                                    }
                                    else{
                                        overAllGlucoseAverage.setText("0");
                                        overAllGlucoseHighest.setText("0");
                                        overAllGlucoseLowest.setText("0");
                                    }
                                }
                            }
                            else{
                                Log.d("TAG", "Task is unsuccessful");
                            }
                        }
                    });

        }
    }
    private void initWidgets(View view) {

        overAllGlucoseAverage = view.findViewById(R.id.glucoseLevelAverage_Textview);
        overAllGlucoseHighest = view.findViewById(R.id.highestGlucoseLevel_Textview);
        overAllGlucoseLowest = view.findViewById(R.id.lowestGlucoseLevel_TxtView);

        viewPager2 = view.findViewById(R.id.afterEating_ViewPager2);
        tabLayout = view.findViewById(R.id.afterEating_TabLayout);
    }
}