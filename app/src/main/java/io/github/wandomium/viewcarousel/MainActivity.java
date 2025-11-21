package io.github.wandomium.viewcarousel;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInPictureInPictureMode()) {
            return;
        }

        mViewPager2 = findViewById(R.id.viewPager);

        /* Handle CAPTURE and RELEASE on view */
        mViewCarousel = new CarouselViewAdapter(Page.loadPages(this), (view) -> {
            if (mViewPager2.isUserInputEnabled()) {
                Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
                mViewPager2.setUserInputEnabled(false);
                findViewById(R.id.menu_btn).setVisibility(View.GONE);
            }
            return false;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mViewPager2.isUserInputEnabled()) {
                    Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
                    mViewPager2.setUserInputEnabled(true);
                    findViewById(R.id.menu_btn).setVisibility(View.VISIBLE);
//                    findViewById(R.id.overlay_view).setVisibility(View.VISIBLE);
                    mViewCarousel.onReleaseFocus();
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
        findViewById(R.id.menu_btn).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
    }

    public void onMenuBtnClicked(View view) {
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());
        Log.d(CLASS_TAG, "BUTTON CLICK");
        PopupMenu menu = new PopupMenu(MainActivity.this, view);
        menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this::_handleMenuSelection);
        menu.show();
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
            private boolean mBlock = false;

            private final Runnable mUnblockInput = new Runnable() {
                @Override
                public void run() {
                    mViewCarousel.blockInput(mBlock);
                }
            };

            @Override
            public void onPageScrollStateChanged(int state) {
                mDragging = state == ViewPager2.SCROLL_STATE_DRAGGING;
                mBlock = state != ViewPager2.SCROLL_STATE_IDLE;

                Log.d(CLASS_TAG, "state: " + state + " block: " + mBlock);

                if (mViewCarousel.isBlocked() && state == ViewPager2.SCROLL_STATE_IDLE) {
                    Log.d(CLASS_TAG, "posting unblock");
                    mMainHandler.removeCallbacks(mUnblockInput);
                    mMainHandler.postDelayed(mUnblockInput, 5000);
                }
                if (mBlock) {
                    mViewCarousel.blockInput(mBlock);
                }

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
