package io.github.wandomium.viewcarousel;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.wandomium.viewcarousel.ui.AFocusMngr;
import io.github.wandomium.viewcarousel.ui.CarouselViewAdapter;

public class MainActivity extends AppCompatActivity
{
    /** @noinspection unused*/
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private CarouselViewAdapter mViewCarousel;
    private CarouselScrollFunc mCarouselScrollCb;
    private ViewPager2 mViewPager2;

    private int mRefreshRate;
    private CarouselViewAdapter.ViewHolder mCurrentViewHolder;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mCurrentViewHolder.reload();
            if (mRefreshRate > 0) {
                mHandler.postDelayed(this, mRefreshRate * 1000L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInPictureInPictureMode()) {
            return;
        }

        if (Settings.getInstance(this).isFirstRun()) {
            new AlertDialog.Builder(this)
                .setMessage("Long press to capture WebPage focus, back to release")
                .setPositiveButton("OK", null)
                .show();
        }

        mViewPager2 = findViewById(R.id.viewPager);
        mViewPager2.setOffscreenPageLimit(4); // TODO: Later we might want to change this or at least make it configurable

        /* Handle CAPTURE and RELEASE on view */
        AFocusMngr focusMngr = _createFocusManager();

        mViewCarousel = new CarouselViewAdapter(
                Page.loadPages(this), focusMngr,
                (pers) -> ActivityCompat.requestPermissions(MainActivity.this, pers, 333));
        getOnBackPressedDispatcher().addCallback(this, focusMngr);
        mViewPager2.setAdapter(mViewCarousel);

        /* Use page scroll cb to implement rotating scroll trough items */
        mCarouselScrollCb = new CarouselScrollFunc(mViewPager2, focusMngr);
        mViewPager2.registerOnPageChangeCallback(mCarouselScrollCb);

        /* Page change to implement refreshing */
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
//                mViewPager2.post(() -> mViewCarousel.setActiveItem(position));
                Page page = mViewCarousel.getPages().get(position);
                mRefreshRate = page != null ? page.refreshRate : 0;
                _startRefreshTask();
            }
        });
    }

    @Override
    protected void onPause() {
        Page.savePages(this, mViewCarousel.getPages());
        _stopRefreshTask();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _startRefreshTask();
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

    // auto enter pip mode
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        _enterPipMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
        findViewById(R.id.menu_btn).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
        findViewById(R.id.call_btn).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
    }

    public void onMenuBtnClicked(View view) {
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());
        PopupMenu menu = new PopupMenu(MainActivity.this, view);
        menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this::_handleMenuSelection);
        menu.show();
    }

    public void onCallBtnClicked(View v) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:"));
        startActivity(dialIntent);
//        if (dialIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(dialIntent);
//        }
    }

    public void onCallBtnClickedTest(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Missing permission", Toast.LENGTH_LONG).show();
        } else {
            // make call
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:+38640655943"));
            intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            startActivity(intent);
        }
    }

    /** @noinspection SameReturnValue*/
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

    private void _startRefreshTask() {
        if (mHandler.hasCallbacks(mRefreshRunnable)) {
            return;
        }
        if (mRefreshRate > 0) {
            mCurrentViewHolder = (CarouselViewAdapter.ViewHolder)
                    ((RecyclerView) mViewPager2.getChildAt(0))
                            .findViewHolderForAdapterPosition(mViewPager2.getCurrentItem());
            mHandler.postDelayed(mRefreshRunnable, mRefreshRate * 1000L);
        }
    }

    private void _stopRefreshTask() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    private AFocusMngr _createFocusManager() {
        return new AFocusMngr() {
            @Override
            protected void _onObtainFocus() {
                Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
                mViewPager2.setUserInputEnabled(false);
                findViewById(R.id.menu_btn).setVisibility(View.GONE);
                findViewById(R.id.call_btn).setVisibility(View.GONE);
            }
            @Override
            protected void _onReleaseFocus() {
                Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
                mViewPager2.setUserInputEnabled(true);
                findViewById(R.id.menu_btn).setVisibility(View.VISIBLE);
                findViewById(R.id.call_btn).setVisibility(View.VISIBLE);
            }
            @Override
            protected void _onBlock() { _stopRefreshTask(); }
            @Override
            protected void _onUnblock() { _startRefreshTask(); }
        };
    }
}
