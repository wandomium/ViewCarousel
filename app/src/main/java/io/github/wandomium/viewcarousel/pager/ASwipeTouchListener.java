package io.github.wandomium.viewcarousel.pager;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public abstract class ASwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector mGestureDetector;

    public ASwipeTouchListener(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    public abstract void onSwipeLeft();
    public abstract void onSwipeRight();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

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
}

