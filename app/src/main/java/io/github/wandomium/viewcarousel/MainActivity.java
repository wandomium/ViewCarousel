package io.github.wandomium.viewcarousel;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import io.github.wandomium.viewcarousel.ui.ViewCarousel;

public class MainActivity extends AppCompatActivity
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private ViewCarousel mViewCarousel;
    private ViewPager2 mViewPager2;
    private ViewPager2.OnPageChangeCallback mCarouselScrollCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInPictureInPictureMode()) {
            return;
        }

        mViewCarousel = new ViewCarousel(Page.loadPages(this));
        mViewPager2 = findViewById(R.id.viewPager);
        mViewPager2.setAdapter(mViewCarousel);

        // TODO: Later we might want to change this or at least make it configurable
        mViewPager2.setOffscreenPageLimit(1);

        _configureCarouselScroll();
    }

    @Override
    protected void onPause() {
        Page.savePages(this, mViewCarousel.getPages());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Page.savePages(this, mViewCarousel.getPages());
        mViewPager2.unregisterOnPageChangeCallback(mCarouselScrollCb);
        mCarouselScrollCb = null;
        mViewPager2 = null;
        mViewCarousel = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();
        int currentPos = mViewPager2.getCurrentItem();

        if (id == R.id.action_add_page) {
            currentPos = mViewCarousel.insertPage(currentPos);
        } else if (id == R.id.action_remove_page) {
            currentPos = mViewCarousel.removePage(currentPos);
        }
        else if (id == R.id.action_enter_pip) {
            enterPipMode();
        }
        mViewPager2.setCurrentItem(currentPos, true);

        return true;
    }

    private void enterPipMode() {
        Rational ratio = new Rational(9, 12);
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(ratio)
                .setSeamlessResizeEnabled(true)
                .build();
        enterPictureInPictureMode(params);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPipMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
//        findViewById(R.id.pipButton).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
    }

    private void _configureCarouselScroll() {
        mCarouselScrollCb = new ViewPager2.OnPageChangeCallback()
        {
            private boolean mDragging = false;

            @Override
            public void onPageScrollStateChanged(int state) {
                mDragging = state == ViewPager2.SCROLL_STATE_DRAGGING;
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mDragging && positionOffset == 0.0) {
                    if (position == 0) {
                        mViewPager2.setCurrentItem(mViewCarousel.getItemCount(), false);
                    } else if (position == mViewCarousel.getItemCount() - 1) {
                        mViewPager2.setCurrentItem(0, false);
                    }
                    return;
                }
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        };
        mViewPager2.registerOnPageChangeCallback(mCarouselScrollCb);
    }
}
