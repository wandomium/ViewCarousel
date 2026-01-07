/**
 * This file is part of ViewCarousel.
 * <p>
 * ViewCarousel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * ViewCarousel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ViewCarousel. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class PageIndicator extends androidx.appcompat.widget.AppCompatTextView
{
    public static final int PAGE_ID_DISPLAY_MS_DEFOPT = 1000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mPageIdAnimation = () -> PageIndicator.this.setVisibility(View.GONE);

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
