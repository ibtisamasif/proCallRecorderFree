package lvc.pro.com.free.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.LinearLayout;

import com.callrecorder.free.R;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import lvc.pro.com.free.Splash_Activity;

public class OnBoardingActivity extends AppCompatActivity {
    private static final String TAG = "OnBoardingActivity";
    private LinearLayout indicator;
    private int mDotCount;
    private LinearLayout[] mDots;
    public static CustomViewPager viewPager;
    private TutorialsFragmentPagerAdapter tutorialsFragmentPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        Fabric.with(this, new Crashlytics());
        final SharedPreferences pref = this.getSharedPreferences("FirstRun", Context.MODE_PRIVATE);
        boolean f1 = pref.getBoolean("isFirstRun", true);
        if (!f1) {
            Intent intent = new Intent(getApplicationContext(), Splash_Activity.class);
            startActivity(intent);
            finish();
        }
        indicator = findViewById(R.id.indicators);
        viewPager = findViewById(R.id.viewpager);
        FragmentManager fm = getSupportFragmentManager();
        tutorialsFragmentPagerAdapter = new TutorialsFragmentPagerAdapter(fm);
        viewPager.setAdapter(tutorialsFragmentPagerAdapter);
        viewPager.setCurrentItem(0);
//        viewPager.setPagingEnabled(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if (position == 1) {
//                    viewPager.setPagingEnabled(false);
//                } else if (position == 2) {
//                    viewPager.setPagingEnabled(false);
//                }

                for (int i = 0; i < mDotCount; i++) {
                    mDots[i].setBackgroundResource(R.drawable.nonselected_item);
                }
                mDots[position].setBackgroundResource(R.drawable.selected_item);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setUiPageViewController();
    }

    private void setUiPageViewController() {
        mDotCount = tutorialsFragmentPagerAdapter.getCount();
        mDots = new LinearLayout[mDotCount];

        for (int i = 0; i < mDotCount; i++) {
            mDots[i] = new LinearLayout(OnBoardingActivity.this);
            mDots[i].setBackgroundResource(R.drawable.nonselected_item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);
            indicator.addView(mDots[i], params);
            mDots[0].setBackgroundResource(R.drawable.selected_item);
        }
    }

    public class TutorialsFragmentPagerAdapter extends FragmentPagerAdapter {

        final int TAB_COUNT = 2;

        TutorialsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = FragmentA.newInstance("title", "page");
                    break;
                case 1:
                    fragment = FragmentB.newInstance("title", "page");
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "A";
                case 1:
                    return "B";
                default:
                    return null;
            }
        }
    }
}
