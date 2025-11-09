package io.github.wandomium.viewcarousel;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import io.github.wandomium.viewcarousel.ui.CarouselViewAdapter;

public class MainActivity extends AppCompatActivity
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private CarouselViewAdapter mViewCarousel;
    private ViewPager2 mViewPager2;
    private ViewPager2.OnPageChangeCallback mCarouselScrollCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInPictureInPictureMode()) {
            return;
        }

        mViewPager2 = findViewById(R.id.viewPager);

        /* Menu button */
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());
        findViewById(R.id.menu_btn).setOnClickListener(v -> {
            Log.d(CLASS_TAG, "BUTTON CLICK");
            PopupMenu menu = new PopupMenu(MainActivity.this, v);
            menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(this::_handleMenuSelection);
            menu.show();
        });

        /* Handle CAPTURE and RELEASE on view */
        mViewCarousel = new CarouselViewAdapter(Page.loadPages(this), (view) -> {
            if (mViewPager2.isUserInputEnabled()) {
                Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
                mViewPager2.setUserInputEnabled(false);
//                ImageButton btn = findViewById(R.id.menu_btn);
//                btn.setEnabled(false);
//                btn.setVisibility(View.GONE);
                findViewById(R.id.overlay_view).setVisibility(View.GONE);
            }
            return false;
        });
        findViewById(R.id.overlay_view).setOnLongClickListener((v) -> {
            if (mViewPager2.isUserInputEnabled()) {
                Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
                mViewPager2.setUserInputEnabled(false);
                findViewById(R.id.overlay_view).setVisibility(View.GONE);
            }
            return false;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mViewPager2.isUserInputEnabled()) {
                    Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
                    mViewPager2.setUserInputEnabled(true);
//                    ImageButton btn = findViewById(R.id.menu_btn);
//                    btn.setEnabled(true);
//                    btn.setVisibility(View.VISIBLE);
                    findViewById(R.id.overlay_view).setVisibility(View.VISIBLE);
                }
            }
        });


        // TODO: Later we might want to change this or at least make it configurable
        mViewPager2.setOffscreenPageLimit(4);
        mViewPager2.setAdapter(mViewCarousel);
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
        mViewPager2.setAdapter(null);
        mViewCarousel = null;
        mViewPager2 = null;
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
        return _handleMenuSelection(item);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        _enterPipMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
//        findViewById(R.id.pipButton).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
    }

    private boolean _handleMenuSelection(MenuItem item) {
        int id = item.getItemId();
        int currentPos = mViewPager2.getCurrentItem();

        if (id == R.id.action_add_page) {
            currentPos = mViewCarousel.insertPage(currentPos);
        } else if (id == R.id.action_remove_page) {
            currentPos = mViewCarousel.removePage(currentPos);
        }
        else if (id == R.id.action_enter_pip) {
            _enterPipMode();
        }
        mViewPager2.setCurrentItem(currentPos, true);

        return true;
    }

    private void _enterPipMode() {
        Rational ratio = new Rational(9, 12);
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(ratio)
                .setSeamlessResizeEnabled(true)
                .build();
        enterPictureInPictureMode(params);
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
