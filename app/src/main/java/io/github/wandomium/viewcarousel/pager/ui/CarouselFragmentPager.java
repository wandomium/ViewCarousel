package io.github.wandomium.viewcarousel.pager.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Objects;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.FragmentBase;

public class CarouselFragmentPager extends FrameLayout
{
    private static final String CLASS_TAG = CarouselFragmentPager.class.getSimpleName();

    public static final int MAX_VIEWS = 5;

    protected static final int RIGHT_IN = 1;
    protected static final int LEFT_IN  = 2;

    private PageIndicator mPageIdDisplay;

    // Fragment list
    private FragmentManager mFragmentMngr;
    private int mCurrentFragment = 0;
    private int mIdCount = 0;
    private String _createNewTag() {
        return "f_" + (mIdCount++);
    }
    private ArrayList<String> mFragmentTags = new ArrayList<>(MAX_VIEWS);

    // Swipe gesture detector - previous, next, focus
    private final GestureDetector mGestureDetector;
    private final int cTouchSlop;
    private boolean mSwipeDetected = false;
    private boolean mCaptureInput  = false;
    private CaptureInputListener mCaptureInputListener;
    @FunctionalInterface
    public interface CaptureInputListener {
        void onCaptureInput();
    }

    public CarouselFragmentPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.carousel_fragment_pager, this,true);

        cTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPageIdDisplay = v.findViewById(R.id.page_indicator);
        mGestureDetector = new GestureDetector(context, new HorizontalSwipeListener());
    }

    public void setFragmentManager(@NonNull FragmentManager fMngr) {
        this.mFragmentMngr = fMngr;
    }
    public void setCaptureInputListener(CaptureInputListener listener) {
        this.mCaptureInputListener = listener;
    }

    public int numFragments() {
        return mFragmentTags.size();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(CLASS_TAG, "onInterceptTouchEvent, capture=" + mCaptureInput);

        if (mCaptureInput) {
            return super.onInterceptTouchEvent(ev);
        }
        mGestureDetector.onTouchEvent(ev);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN -> {
//                return false; // pass to child
//            }
//            case MotionEvent.ACTION_MOVE -> {
//                if (mSwipeDetected) return true; // consume. events are dispatched to onTouchEvent
//            }
//        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE && mSwipeDetected) {
            return true;
        }
        else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // when intercept returns true full event stream comes here
        return mGestureDetector.onTouchEvent(ev);
    }

    /* The primary cleanup method. Called when the view is removed from the window hierarchy
     * (e.g., when its hosting Activity/Fragment is destroyed). This is where you should unregister listeners and nullify references.
     */
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void captureInput(final boolean capture) {
        Log.d(CLASS_TAG, "captureInput: " + capture);
        mCaptureInput = capture;
        _getCurrentFragment().captureInput(capture);
        if (mCaptureInputListener != null && capture) {
            mCaptureInputListener.onCaptureInput();
        }
    }


    // todo - have append fragment to replace this
    public int currentFragment() {
        return mCurrentFragment;
    }

    public boolean addFragment(final int position, @NonNull Fragment f) throws IllegalArgumentException {
        if (position < 0 || position >= MAX_VIEWS) {
            final String msg = "Cannot add fragment. Position out of bounds: " + position;
            throw new IllegalArgumentException(msg);
        }
        if (mFragmentTags.size() >= MAX_VIEWS) {
            final String msg = "Cannot at fragment. Max view limit reached: " + MAX_VIEWS;
            throw new IllegalArgumentException(msg);
        }

        final String fTag = _createNewTag();
        if (position == mFragmentTags.size()) {
            mFragmentTags.add(fTag);
        }
        else {
            mFragmentTags.add(position, fTag);
        }

        // todo - is commitNow the best thing here?
        // TODO - have switch to new fragment option and use commitNow in this case
        if (mFragmentTags.size() == 1) {
            mFragmentMngr.beginTransaction().replace(R.id.fragment_container, f, fTag).commitNow();
        }
        else {
            mFragmentMngr.beginTransaction().add(R.id.fragment_container, f, fTag).hide(f).commitNow();
        }

        // TODO: show the added fragment?? probably
        Log.d(CLASS_TAG, mFragmentTags.toString());
        Log.d(CLASS_TAG, mFragmentMngr.getFragments().toString());
        return true;
    }

    public void removeFragment(final int position, boolean removeAndSwitch) throws IllegalArgumentException {
        try {
            final String fTag = mFragmentTags.get(position);
            final Fragment fRemove = mFragmentMngr.findFragmentByTag(fTag);
            // TODO null checks and commit transaction
            if (position == mCurrentFragment && removeAndSwitch) {
                // TODO do we need the commitNow?
                //mFragmentMngr.beginTransaction().remove().show().commitNow();
                _switchFragment(mCurrentFragment, _previousFragment(), LEFT_IN, true);
            }
            else {
                mFragmentMngr.beginTransaction().remove(fRemove).commitNow();
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            final String msg = "Trying to remove non-existing fragment: " + e.getClass().getSimpleName();
            throw new IllegalArgumentException(msg);
        }
        mFragmentTags.remove(position);
        Log.d(CLASS_TAG, "Removed, remaining: " + mFragmentTags.toString());
    }

    public void replaceFragment(final int position, Fragment fNew) throws IllegalArgumentException {
        try {
            // TODO null checks and commit transaction
            final Fragment fRemove = mFragmentMngr.findFragmentByTag(mFragmentTags.get(position));
            final String fNewTag = _createNewTag();
            FragmentTransaction fTransaction =
                mFragmentMngr.beginTransaction().remove(fRemove).add(R.id.fragment_container, fNew, fNewTag);
            if (position != mCurrentFragment) {
                fTransaction.hide(fNew);
            }
            fTransaction.commitNow();
            mFragmentTags.set(position, fNewTag);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            final String msg = "Trying to remove non-existing fragment: " + e.getClass().getSimpleName();
            throw new IllegalArgumentException(msg);
        }
        Log.d(CLASS_TAG, "Removed, remaining: " + mFragmentTags.toString());
    }


    private int _previousFragment() {
        return (mCurrentFragment == 0) ? mFragmentTags.size() - 1 : mCurrentFragment - 1;
    }
    private int _nextFragment() {
        return (mCurrentFragment == mFragmentTags.size() - 1) ? 0 : mCurrentFragment + 1;
    }
    public boolean showFragment(final int position) {
        if (mFragmentMngr.executePendingTransactions()) {
            // we have pendind transactions. state might not be as expected
            return false;
        }
        int direction;
        if (position == 0 && mCurrentFragment == mFragmentTags.size() - 1) {
            direction = LEFT_IN;
        }
        else if (position == mFragmentTags.size() - 1 && currentFragment() == 0) {
            direction = RIGHT_IN;
        }
        else {
            direction = position < mCurrentFragment ? LEFT_IN : RIGHT_IN;
        }

        _switchFragment(position, direction);

        return true;
    }

    protected void onSwipeLeft() { // == swipe next
        final int to = (mCurrentFragment == mFragmentTags.size() - 1) ? 0 : mCurrentFragment+1;
        _switchFragment(to, RIGHT_IN);
    }

    protected void onSwipeRight() {
        final int to = (mCurrentFragment == 0) ? mFragmentTags.size() - 1 : mCurrentFragment-1;
        _switchFragment(to, LEFT_IN);
    }

    protected void onSwipeUp() {
        Log.d(CLASS_TAG, "onSwipeUp");
        captureInput(true);
    }

    private class HorizontalSwipeListener extends GestureDetector.SimpleOnGestureListener
    {
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent ev) {
            mSwipeDetected = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (!mSwipeDetected) {
                final float absDiffX = Math.abs(e2.getX() - e1.getX());
                final float diffY = e2.getY() - e1.getY();
                final float absDiffY = Math.abs(diffY);

                if ((absDiffY > cTouchSlop && diffY < 0 ) || // upwards swipe
                    (absDiffX > cTouchSlop && absDiffX > absDiffY)) // horizontal swipe
                {
                    mSwipeDetected = true;
                    return true;
                }
            }
            return mSwipeDetected; // If already intercepting, continue consuming.
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (mSwipeDetected) {
                final float diffX = e2.getX() - e1.getX();
                final float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) { onSwipeRight(); }
                    else { onSwipeLeft(); }
                    mSwipeDetected = false; // Reset after handling
                    return true;
                }
                else if (diffY < 0 && velocityY < 0) {
                    if (Math.abs(diffY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        onSwipeUp();
                        mSwipeDetected = false;
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private FragmentBase _getCurrentFragment() {
        // O(N) lookup but we have < 10 fragments
        return (FragmentBase) mFragmentMngr.findFragmentByTag(mFragmentTags.get(mCurrentFragment));
    }
    private void _switchFragment(int to, int direction) {
        _switchFragment(mCurrentFragment, to, direction, false);
    }
    private void _switchFragment(int from, int to, int direction, boolean remove) {
        Log.d(CLASS_TAG, "switch: " + from + " -> " + to);
        Log.d(CLASS_TAG, mFragmentTags.toString());
        // TODO: pop enter animations
        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction();
        final FragmentBase fFrom = (FragmentBase) Objects.requireNonNull(
                mFragmentMngr.findFragmentByTag(mFragmentTags.get(from)));
        final FragmentBase fTo = (FragmentBase) Objects.requireNonNull(
                mFragmentMngr.findFragmentByTag(mFragmentTags.get(to)));
        switch (direction) {
            case RIGHT_IN -> fTransaction.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left);
            case LEFT_IN -> fTransaction.setCustomAnimations(
                    R.anim.slide_in_left, R.anim.slide_out_right);
            default -> throw new IllegalArgumentException(
                    "Unknown transition direction");
        }
        if (remove) {
            fTransaction.remove(fFrom);
        } else {
            fTransaction.hide(fFrom);
        }
        fFrom.onHide();
        fTransaction.show(fTo);
        fFrom.onShow();
        fTransaction.disallowAddToBackStack();

        mCurrentFragment = to;
        mPageIdDisplay.showPageIndicator(mCurrentFragment+1, mFragmentTags.size());

        fTransaction.commitNow();
    }
}
