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
import com.example.dpatient.ReportsFasting.FastingReports.MyViewPagerAdapterFastingReports;
import com.example.dpatient.UserDetails;
import com.example.dpatient.Utils.DateAndTimeUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class FastingFragment extends Fragment {
    private TextView overAllGlucoseAverage;
    private TextView overAllGlucoseHighest;
    private TextView overAllGlucoseLowest;
    private UserDetails userDetails;
    private String patientId;
    private MyViewPagerAdapterFastingReports myViewPagerAdapter;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fasting, container, false);
        initWidgets(view);

        setUpAllOverData();

        myViewPagerAdapter = new MyViewPagerAdapterFastingReports(getActivity());
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

        return  view;
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
                                    if (documentSnapshot.contains("fasting_average") &&
                                            documentSnapshot.contains("fasting_highest") &&
                                            documentSnapshot.contains("fasting_lowest")){

                                        long fastingGlucoseAverage = documentSnapshot.getLong("fasting_average");
                                        long fastingHighestGlucose = documentSnapshot.getLong("fasting_highest");
                                        long fastingLowestGlucose = documentSnapshot.getLong("fasting_lowest");

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

        viewPager2 = view.findViewById(R.id.fasting_ViewPager2);
        tabLayout = view.findViewById(R.id.fasting_TabLayout);
    }
}