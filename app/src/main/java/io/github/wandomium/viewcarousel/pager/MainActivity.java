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

        for (int i = 0; i < CarouselFragmentPager.MAX_VIEWS; i++) {
            pager.addFragment(BaseFragment.createFragment(i), i);
        }
    }
}
