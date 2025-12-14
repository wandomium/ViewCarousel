package io.github.wandomium.viewcarousel.pager.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Objects;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.FragmentBase;
import io.github.wandomium.viewcarousel.pager.data.Page;

public class CarouselFragmentPager extends FrameLayout
{
    private static final String CLASS_TAG = CarouselFragmentPager.class.getSimpleName();

    public static final int MAX_VIEWS = 5;

    protected enum Direction {
        RIGHT_IN, LEFT_IN
    }

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
    private final SwipeDetectorListener mSwipeDetectorListener;
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

        mPageIdDisplay = v.findViewById(R.id.page_indicator);
        mSwipeDetectorListener = new SwipeDetectorListener(
            ViewConfiguration.get(context).getScaledTouchSlop(), (direction) -> {
                switch (direction) {
                    case SwipeDetectorListener.SWIPE_LEFT -> _switchFragment(_nextFragment(), Direction.RIGHT_IN, false);
                    case SwipeDetectorListener.SWIPE_RIGHT -> _switchFragment(_previousFragment(), Direction.LEFT_IN, false);
                    case SwipeDetectorListener.SWIPE_UP -> captureInput(true);
                }
        });
        mGestureDetector = new GestureDetector(getContext(), mSwipeDetectorListener);
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
    // todo - have append fragment to replace this
    public int currentFragment() {
        return mCurrentFragment;
    }
    public void notifyFragmentDataChanged(int framgentIdx, Page newData) {
        try {
            ((FragmentBase) mFragmentMngr.findFragmentByTag(mFragmentTags.get(framgentIdx))).updateData(newData);
        }
        catch (NullPointerException | IndexOutOfBoundsException e) {
            Log.e(CLASS_TAG, "Could not get fragment for fragment id " + framgentIdx);
        }
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
        if (ev.getAction() == MotionEvent.ACTION_MOVE && mSwipeDetectorListener.swipeInProcess()) {
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

    public boolean addFragment(final int position, @NonNull Fragment f) throws IllegalArgumentException {
        if (mFragmentTags.size() >= MAX_VIEWS) {
            final String msg = "Cannot at fragment. Max view limit reached: " + MAX_VIEWS;
            throw new IllegalArgumentException(msg);
        }
        if (position < 0 || position > mFragmentTags.size()) {
            final String msg = "Cannot add fragment. Invalid position: " + position + " of total " + mFragmentTags.size();
            throw new IllegalArgumentException(msg);
        }

        final String fTag = _createNewTag();
        mFragmentTags.add(position, fTag);

        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction().add(R.id.fragment_container, f, fTag);
        if (mFragmentTags.size() > 1) {
            fTransaction.hide(f);
        }
        // use commitNow so that fragments are immediately available
        fTransaction.commitNow();

        // TODO: show the added fragment?? probably
        Log.d(CLASS_TAG, mFragmentTags.toString());
        Log.d(CLASS_TAG, mFragmentMngr.getFragments().toString());
        return true;
    }

    public void removeFragment(final int position) throws IllegalArgumentException {
        try {
            if (position == mCurrentFragment) {
                _switchFragment(_previousFragment(), Direction.LEFT_IN, true);
            }
            else {
                // TODO do we need the commitNow? probably not for remove
                final Fragment fRemove = mFragmentMngr.findFragmentByTag(mFragmentTags.get(position));
                mFragmentMngr.beginTransaction().remove(fRemove).commitNow();
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            final String msg = "Trying to remove non-existing fragment: " + e.getClass().getSimpleName();
            throw new IllegalArgumentException(msg);
        }
        mFragmentTags.remove(position);
        Log.d(CLASS_TAG, "Removed, remaining: " + mFragmentTags.toString() + " current=" + mCurrentFragment);
    }

    public void replaceFragment(final int position, Fragment fNew) throws IllegalArgumentException {
        try {
            final Fragment fRemove = mFragmentMngr.findFragmentByTag(mFragmentTags.get(position));
            final String fNewTag = _createNewTag();
            FragmentTransaction fTransaction =
                mFragmentMngr.beginTransaction().remove(fRemove).add(R.id.fragment_container, fNew, fNewTag);
            if (position != mCurrentFragment) {
                fTransaction.hide(fNew); // if replacing at current position, keep fragment visible
            }
            fTransaction.commitNow();
            mFragmentTags.set(position, fNewTag);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            final String msg = "Trying to remove non-existing fragment: " + e.getClass().getSimpleName();
            throw new IllegalArgumentException(msg);
        }
        Log.d(CLASS_TAG, "Removed, remaining: " + mFragmentTags.toString());
    }

    public boolean showFragment(final int position) {
        if (mFragmentMngr.executePendingTransactions()) {
            // we have pendind transactions. state might not be as expected
            return false;
        }
        Direction direction;
        if (position == 0 && mCurrentFragment == mFragmentTags.size() - 1) {
            direction = Direction.LEFT_IN;
        }
        else if (position == mFragmentTags.size() - 1 && mCurrentFragment == 0) {
            direction = Direction.RIGHT_IN;
        }
        else {
            direction = position < mCurrentFragment ? Direction.LEFT_IN : Direction.RIGHT_IN;
        }

        _switchFragment(position, direction, false);

        return true;
    }

    private int _previousFragment() {
        return (mCurrentFragment == 0) ? mFragmentTags.size() - 1 : mCurrentFragment - 1;
    }
    private int _nextFragment() {
        return (mCurrentFragment == mFragmentTags.size() - 1) ? 0 : mCurrentFragment + 1;
    }
    private FragmentBase _getCurrentFragment() {
        // O(N) lookup but we have < 10 fragments
        return (FragmentBase) mFragmentMngr.findFragmentByTag(mFragmentTags.get(mCurrentFragment));
    }

    private void _switchFragment(int to, Direction direction, boolean replaceCurrent) {
        Log.d(CLASS_TAG, "switch: " + mCurrentFragment + " -> " + to);
        Log.d(CLASS_TAG, mFragmentTags.toString());
        // TODO: pop enter animations
        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction();
        final FragmentBase fFrom = (FragmentBase) Objects.requireNonNull(
                mFragmentMngr.findFragmentByTag(mFragmentTags.get(mCurrentFragment)));
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
        if (replaceCurrent) {
            //on inplace switch, current fragment id stays
            fTransaction.remove(fFrom);
        } else {
            fTransaction.hide(fFrom);
            mCurrentFragment = to;
        }
        fFrom.onHide();
        fTransaction.show(fTo);
        fFrom.onShow();
        fTransaction.disallowAddToBackStack();

        int numpPages = mFragmentTags.size();
        if (replaceCurrent) {
            numpPages--;
        }
        mPageIdDisplay.showPageIndicator(mCurrentFragment+1, numpPages);

        // TODO: we probably don't need commitNow here. only when adding new fragments
        fTransaction.commitNow();
    }
}
