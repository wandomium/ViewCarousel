package io.github.wandomium.viewcarousel.pager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import io.github.wandomium.viewcarousel.R;

public class MainActivity extends AppCompatActivity
{
    private OnBackPressedCallback mBackPressedCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity_main);

        CarouselFragmentPager pager = findViewById(R.id.carousel_pager);
        pager.setFragmentManager(getSupportFragmentManager());
        pager.setCaptureInputListener(() -> {
            mBackPressedCb.setEnabled(true);
            Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
        });

        mBackPressedCb = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                pager.captureInput(false);
                setEnabled(false); // system handled back gesture
                Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(mBackPressedCb);

        int i = 0;
        for (; i < CarouselFragmentPager.MAX_VIEWS - 1; i++) {
            pager.addFragment(BaseFragment.createFragment(i, BaseFragment.FRAGMENT_NEW_PAGE), i);
        }
        FWebPage wp = (FWebPage) BaseFragment.createFragment(i, BaseFragment.FRAGMENT_WEB_PAGE);
        wp.setUrl("https://archlinux.org");
        pager.addFragment(wp, i++);
    }
}
