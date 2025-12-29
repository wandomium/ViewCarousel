package io.github.wandomium.viewcarousel.pager.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class SwipeDetectorListener extends GestureDetector.SimpleOnGestureListener
{
    private static final String CLASS_TAG = SwipeDetectorListener.class.getSimpleName();

    public static final int SWIPE_LEFT  = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_UP    = 3;
    public static final int SWIPE_DOWN  = 4; // unused, for completeness
    public static final int SWIPE_2FINGER_UP = 5; // unused, for completeness
    public static final int SWIPE_2FINGER_DOWN = 6; // this is a continuous motion


    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 20;

    private final int cTouchSlop;
    private final SwipeCallback mSwipeCb;
    private boolean mSwipeInProcess = false;
    private boolean mTwoFingerSwipe = false;

    private float mDx;
    private float mDy;

    @FunctionalInterface
    public interface SwipeCallback {
        // while this method is returning true, we consume the gesture
        boolean onSwipe(final int direction, final float distance);
    }

    public SwipeDetectorListener(int touchSlop, SwipeCallback cb) {
        this.cTouchSlop = touchSlop;
        this.mSwipeCb = cb;
    }

    public boolean swipeInProgress() {
        return mSwipeInProcess;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent ev) {
        mSwipeInProcess = false;
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        mDx = e2.getX() - e1.getX();
        mDy = e2.getY() - e1.getY();
        if (!mSwipeInProcess) {
            final float dx_abs = Math.abs(e2.getX() - e1.getX());
            final float dy_abs = Math.abs(mDy);

            mTwoFingerSwipe = e1.getPointerCount() == 2 || e2.getPointerCount() == 2; //when finishing, none of these are 2
            if ((dy_abs > cTouchSlop && mDy < 0 ) || // upwards swipe
                (dy_abs > cTouchSlop && mDy > 0 && mTwoFingerSwipe) || // two finger down swipe to open the menu
                (dx_abs > cTouchSlop && dx_abs > dy_abs)) // horizontal swipe
            {
                mSwipeInProcess = true;
                return true;
            }
        }
        // TODO: for later, when we want to track swipe and show the menu nicely
//        if (mSwipeInProccess && mTwoFingerSwipe && mDy > 0) {
//            mSwipeInProccess = mSwipeCb.onSwipe(SWIPE_2FINGER_DOWN, mDy);
//        }

        return mSwipeInProcess; // If already intercepting, continue consuming.
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        mDx = e2.getX() - e1.getX();
        mDy = e2.getY() - e1.getY();
        if (mSwipeInProcess) {
            // reset, fling done
            mSwipeInProcess = false;

            // swipe up/down for capture/menu
            if (Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD && _isUpOrDownSwipe(SWIPE_DISTANCE_THRESHOLD)) {
                return true;
            }
            // swipe left/right
            else if (Math.abs(mDx) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (mDx > 0) { mSwipeCb.onSwipe(SWIPE_RIGHT, mDx); }
                else { mSwipeCb.onSwipe(SWIPE_LEFT, mDx); }
                return true;
            }
        }
        return false;
    }

    // Support long swipe up (for capture) and long swipe down
    public boolean onUp(MotionEvent e) {
        if (mSwipeInProcess) {
            mSwipeInProcess = false;
            return _isUpOrDownSwipe(700);
        }
        return false;
    }

    // helper to detect swipe up/down for fling or long swipe
    private boolean _isUpOrDownSwipe(int threshold) {

        if ((Math.abs(mDy)/Math.abs(mDx)) > 0.5 // so that the swipe can be a bit diagonal
                && Math.abs(mDy) > threshold)
        {
            if (mDy < 0) {
                mSwipeCb.onSwipe(mTwoFingerSwipe ? SWIPE_2FINGER_UP : SWIPE_UP, mDy);
                return true;
            }
            else if (mDy > 0) {
                mSwipeCb.onSwipe(mTwoFingerSwipe ? SWIPE_2FINGER_DOWN : SWIPE_DOWN, mDy);
                return true;
            }
        }
        return false;
    }
}
