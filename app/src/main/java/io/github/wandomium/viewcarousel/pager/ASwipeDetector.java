package io.github.wandomium.viewcarousel.pager;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;


public abstract class ASwipeDetector extends GestureDetector.SimpleOnGestureListener
{
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public abstract void onSwipeRight();
    public abstract void onSwipeLeft();

    @Override
    public boolean onDown(@NonNull MotionEvent ignored) {
        return true; // Must return true to detect other gestures
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
