package io.github.wandomium.viewcarousel.pager.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class SwipeDetectorListener extends GestureDetector.SimpleOnGestureListener
{
    public static final int SWIPE_LEFT  = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_UP    = 3;


    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 20;

    private final int cTouchSlop;
    private final SwipeCallback mSwipeCb;
    private boolean mSwipeInProccess = false;

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
        if (!mSwipeInProccess) {
            final float absDiffX = Math.abs(e2.getX() - e1.getX());
            final float diffY = e2.getY() - e1.getY();
            final float absDiffY = Math.abs(diffY);

            if ((absDiffY > cTouchSlop && diffY < 0 ) || // upwards swipe
                    (absDiffX > cTouchSlop && absDiffX > absDiffY)) // horizontal swipe
            {
                mSwipeInProccess = true;
                return true;
            }
        }
        return mSwipeInProccess; // If already intercepting, continue consuming.
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        if (mSwipeInProccess) {
            final float diffX = e2.getX() - e1.getX();
            final float diffY = e2.getY() - e1.getY();

            if ((Math.abs(diffY)/Math.abs(diffX)) > 0.5  && (diffY < 0 && velocityY < 0)) {
                if (Math.abs(diffY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    mSwipeCb.onSwipe(SWIPE_UP);
                    mSwipeInProccess = false;
                    return true;
                }
            }
            else if (Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) { mSwipeCb.onSwipe(SWIPE_RIGHT); }
                else { mSwipeCb.onSwipe(SWIPE_LEFT); }
                mSwipeInProccess = false; // Reset after handling
                return true;
            }
        }
        return false;
    }
}
