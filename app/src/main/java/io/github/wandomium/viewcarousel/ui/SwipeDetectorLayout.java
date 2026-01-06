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
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import io.github.wandomium.viewcarousel.R;

public class SwipeDetectorLayout extends ConstraintLayout implements ICaptureInput
{
    private static final String CLASS_TAG = SwipeDetectorLayout.class.getSimpleName();
    // Top Menu
    private LinearLayout mTopMenu;

    // Swipe gesture detector - previous, next, focus
    private GestureDetector mGestureDetector;
    private SwipeDetectorListener mSwipeDetectorListener;
    private boolean mCaptureInput  = false;

    public SwipeDetectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.user_actions_detector, this,true);

        ///// PLACEHOLDER
        mTopMenu = v.findViewById(R.id.top_menu);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!(getContext() instanceof SwipeDetectorListener.SwipeCallback swipeCallback)) {
            throw new IllegalArgumentException("RootLayout needs to be instantiated from MainActivity");
        }

        mSwipeDetectorListener = new SwipeDetectorListener(
                ViewConfiguration.get(getContext()).getScaledTouchSlop(), swipeCallback);
        mGestureDetector = new GestureDetector(getContext(), mSwipeDetectorListener);
    }

    /* Called when the view is removed from the window hierarchy
     * (e.g., when its hosting Activity/Fragment is destroyed).
     */
    @Override
    public void onDetachedFromWindow() {
        mSwipeDetectorListener = null; // holds implicit reference to mMainActivity
        mGestureDetector = null;
        mTopMenu = null;
        super.onDetachedFromWindow();
    }


    /////// SWIPES
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(CLASS_TAG, "onInterceptTouchEvent, capture=" + mCaptureInput + ", action=" + ev.getAction());

        if (!mCaptureInput) {
            // if input is not captured, check for gestures
            mGestureDetector.onTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_MOVE && mSwipeDetectorListener.swipeInProgress())
            {
                return true; // consume event
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // when intercept returns true full event stream comes here
        // return mGestureDetector.onTouchEvent(ev);
        final int action = ev.getAction();
        final boolean retval = mGestureDetector.onTouchEvent(ev);

        if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                && mSwipeDetectorListener.swipeInProgress())
        {
            return mSwipeDetectorListener.onSwipeDone(ev);
        }
        return retval;
    }

    @Override
    public boolean setCaptureInput(final boolean captureReq) {
        mCaptureInput = captureReq;
        return mCaptureInput;
    }

    /// CURRENTLY UNUSED, FOR TOP MENU
//    private boolean _showTopMenu(final float distance) {
//        final int height = mTopMenu.getHeight();
//        final boolean retval =  distance < height;
//        if (retval) {
//            mTopMenu.setTranslationY(distance-height);
//        }
//        return retval;
//    }
//
//    private boolean _hideTopMenu() {
//        mTopMenu.setTranslationY(-mTopMenu.getHeight());
//        return false;
//    }
    /// ///////////////
}
