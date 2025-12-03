package io.github.wandomium.viewcarousel.pager.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class PageIndicator extends androidx.appcompat.widget.AppCompatTextView
{
    public static final int PAGE_ID_DISPLAY_MS_DEFOPT = 1000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mPageIdAnimation = new Runnable() {
        @Override
        public void run() {
            PageIndicator.this.setVisibility(View.GONE);
        }
    };

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void showPageIndicator(int idx, int total) {
        showPageIndicator(idx, total, PAGE_ID_DISPLAY_MS_DEFOPT);
    }

    public void showPageIndicator(int idx, int total, int tout_ms) {
        mHandler.removeCallbacks(mPageIdAnimation);
        this.setText(idx + "/" + total);
        this.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mPageIdAnimation, tout_ms);
    }

    @Override
    public void onDetachedFromWindow() {
        mHandler.removeCallbacks(mPageIdAnimation);
        super.onDetachedFromWindow();
    }
}
