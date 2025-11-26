package io.github.wandomium.viewcarousel.demo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class DemoMainActivity extends AppCompatActivity {
    private static final String CLASS_TAG = DemoMainActivity.class.getSimpleName();

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoFragmentStateAdapter demoCollectionAdapter;
    ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_main);

        demoCollectionAdapter = new DemoFragmentStateAdapter(this);

        viewPager2 = findViewById(R.id.pager);
        viewPager2.setAdapter(demoCollectionAdapter);
        viewPager2.setOffscreenPageLimit(7);
//        viewPager2.setCurrentItem(1, false);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText("OBJECT " + (position))
        ).attach();


        ViewPager2.OnPageChangeCallback cb = new ViewPager2.OnPageChangeCallback() {
            private boolean scrollDone = false;
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    scrollDone = true;
                }
            }
            @Override
            public void onPageSelected(int position) {
                if (!scrollDone) {
                    return;
                }
                final int numItems = viewPager2.getAdapter().getItemCount();

                if (position == numItems - 1) {
                    viewPager2.setCurrentItem(1, false);
                }
                else if (position == 0) {
                    viewPager2.setCurrentItem(numItems - 2, false);
                }
                scrollDone = true;
            }
        };


        viewPager2.registerOnPageChangeCallback(cb);
    }

    private int _getPosition(int position) {
        if (position == 0) {
            return viewPager2.getAdapter().getItemCount() - 2;
        }
        else if  (position == viewPager2.getAdapter().getItemCount() - 1) {
            return 1;
        }
        else {
            return position;
        }
    }
}