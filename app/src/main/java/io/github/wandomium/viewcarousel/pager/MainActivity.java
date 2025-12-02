package io.github.wandomium.viewcarousel.pager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.wandomium.viewcarousel.R;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity_main);

        CarouselFragmentPager pager = findViewById(R.id.carousel_pager);
        pager.setFragmentManager(getSupportFragmentManager());

        int i = 0;
        for (; i < CarouselFragmentPager.MAX_VIEWS - 1; i++) {
            pager.addFragment(BaseFragment.createFragment(i, BaseFragment.FRAGMENT_NEW_PAGE), i);
        }
        FWebPage wp = (FWebPage) BaseFragment.createFragment(i, BaseFragment.FRAGMENT_WEB_PAGE);
        wp.setUrl("https://archlinux.org");
        pager.addFragment(wp, i++);
    }
}
