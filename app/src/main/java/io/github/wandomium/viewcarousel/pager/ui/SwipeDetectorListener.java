package io.github.wandomium.viewcarousel.pager.ui;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class SwipeDetectorListener extends GestureDetector.SimpleOnGestureListener
{
    private static final String CLASS_TAG = SwipeDetectorListener.class.getSimpleName();

    public static final int SWIPE_LEFT  = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_UP    = 3;
    public static final int SWIPE_DOWN_2FINGER = 4;


    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 20;

    private final int cTouchSlop;
    private final SwipeCallback mSwipeCb;
    private boolean mSwipeInProccess = false;
    private boolean mTwoFingerSwipe = false;

    private float mDx;
    private float mDy;

    @FunctionalInterface
    public interface SwipeCallback {
        void onSwipe(final int direction);
    }

    public SwipeDetectorListener(int touchSlop, SwipeCallback cb) {
        this.cTouchSlop = touchSlop;
        this.mSwipeCb = cb;
    }

    public boolean swipeInProcess() {
        return mSwipeInProccess;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent ev) {
        mSwipeInProccess = false;
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        mDx = e2.getX() - e1.getX();
        mDy = e2.getY() - e1.getY();
        if (!mSwipeInProccess) {
            final float dx_abs = Math.abs(e2.getX() - e1.getX());
            final float dy = e2.getY() - e1.getY();
            final float dy_abs = Math.abs(dy);

            mTwoFingerSwipe = e1.getPointerCount() == 2 || e2.getPointerCount() == 2;
            if ((dy_abs > cTouchSlop && dy < 0 ) || // upwards swipe
                (dy_abs > cTouchSlop && dy > 0 && mTwoFingerSwipe) || // two finger down swipe
                (dx_abs > cTouchSlop && dx_abs > dy_abs)) // horizontal swipe
            {
                mSwipeInProccess = true;
                return true;
            }
        }

        return mSwipeInProccess; // If already intercepting, continue consuming.
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        mDx = e2.getX() - e1.getX();
        mDy = e2.getY() - e1.getY();
        if (mSwipeInProccess) {
            // swipe up/down for capture/menu
            if (Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD && _isUpOrDownSwipe(SWIPE_DISTANCE_THRESHOLD)) {
                return true;
            }
            // swipe left/right
            else if (Math.abs(mDx) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (mDx > 0) { mSwipeCb.onSwipe(SWIPE_RIGHT); }
                else { mSwipeCb.onSwipe(SWIPE_LEFT); }
                mSwipeInProccess = false;
                return true;
            }
        }
        return false;
    }

    // we want to support long swipe up and long swipe down
    public boolean onUp(MotionEvent e) {
        if (mSwipeInProccess) {
            return _isUpOrDownSwipe(700);
        }
        return false;
    }

    // We want to enable booth quick and long swipe up/down
    private boolean _isUpOrDownSwipe(int treshold) {
        final float dx = mDx;
        final float dy = mDy;

        if ((Math.abs(dy)/Math.abs(dx)) > 0.5 && Math.abs(dy) > treshold) {
                // swipe up
                if (dy < 0) {  //&& velocityY < 0)
                    mSwipeCb.onSwipe(SWIPE_UP);
                    mSwipeInProccess = false;
                    return true;
                }
                else if (dy > 0 && mTwoFingerSwipe) { //&& velocityY > 0
                    mSwipeCb.onSwipe(SWIPE_DOWN_2FINGER);
                    mSwipeInProccess = false;
                    return true;
                }
        }
        return false;
    }
}
