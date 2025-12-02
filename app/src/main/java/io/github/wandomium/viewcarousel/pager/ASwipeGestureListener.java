package io.github.wandomium.viewcarousel.pager;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public abstract class ASwipeGestureListener extends GestureDetector.SimpleOnGestureListener
{
    private static final String CLASS_TAG = ASwipeGestureListener.class.getSimpleName();

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public abstract void onSwipeLeft();
    public abstract void onSwipeRight();

    @Override
    public boolean onDown(@NonNull MotionEvent ignored) {
        Log.d(CLASS_TAG, "onDown");
        return true; // Must return true to detect other gestures
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(CLASS_TAG, "onFling");
        if (e1 == null || e2 == null) return false;

        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) onSwipeRight(); else onSwipeLeft();
                return true;
            }
        }
        return false;
    }
}
