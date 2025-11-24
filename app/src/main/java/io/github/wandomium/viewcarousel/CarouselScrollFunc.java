package io.github.wandomium.viewcarousel;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import io.github.wandomium.viewcarousel.ui.AFocusHandler;

public class CarouselScrollFunc extends ViewPager2.OnPageChangeCallback
{
    private final ViewPager2 mViewPager2;
    private final AFocusHandler mFocusHandler;

    private boolean mDragging = false;
    private boolean mBlock = false;

    private final Handler mMainHandler;
    private final Runnable mUnblockInput;

    public CarouselScrollFunc(@NonNull ViewPager2 viewPager2, @Nullable AFocusHandler focusHandler) {
        this.mViewPager2   = viewPager2;
        this.mFocusHandler = focusHandler;
        this.mMainHandler  = (mFocusHandler != null) ? new Handler(Looper.getMainLooper()) : null;
        this.mUnblockInput = (mFocusHandler != null) ? () -> mFocusHandler.blockInput(mBlock) : null;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mDragging = state == ViewPager2.SCROLL_STATE_DRAGGING;
        mBlock    = state != ViewPager2.SCROLL_STATE_IDLE;

        if (mFocusHandler != null) {
            if (mFocusHandler.isBlocked() && state == ViewPager2.SCROLL_STATE_IDLE) {
                // Post delayed release, otherwise we trigger capture on the view (longClick) while
                // moving with setCurrentItem
                mMainHandler.removeCallbacks(mUnblockInput);
                mMainHandler.postDelayed(mUnblockInput, 5000);
            }
            if (mBlock) {
                mFocusHandler.blockInput(mBlock);
            }
        }

        super.onPageScrollStateChanged(state);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // hack that enables circular scroll on first and last item
        if (mDragging && positionOffset == 0.0 && mViewPager2.getAdapter() != null) {
            final int itemCount = mViewPager2.getAdapter().getItemCount();
            if (position == 0) {
                mViewPager2.setCurrentItem(itemCount, false);
            } else if (position == itemCount - 1) {
                mViewPager2.setCurrentItem(0, false);
            }
            return;
        }

        // we are not on last or first item, continue normally
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }
}
