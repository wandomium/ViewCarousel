package io.github.wandomium.viewcarousel.pager;

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

import io.github.wandomium.viewcarousel.R;

public class CarouselFragmentPager extends FrameLayout
{
    private static final String CLASS_TAG = CarouselFragmentPager.class.getSimpleName();

    public static final int MAX_VIEWS = 5;

    private static final int PAGE_ID_DISPLAY_MS = 1000;

    private static final int RIGHT_IN = 1;
    private static final int LEFT_IN = 2;

    private FragmentManager mFragmentMngr;

    private final GestureDetector mGestureDetector;
    private final int cTouchSlop;

    private boolean mSwipeDetected = false;
    private int mCurrentFragment = 0;

    private int mIdCount = 0;
    private String _createNewTag() {
        return "f_" + (mIdCount++);
    }
    private ArrayList<String> mFragmentTags = new ArrayList<>(MAX_VIEWS);

    private TextView mPageIdDisplay;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mPageIdAnimation = new Runnable() {
        @Override
        public void run() {
            mPageIdDisplay.setVisibility(View.GONE);
        }
    };

    public CarouselFragmentPager(Context context, AttributeSet attrs) {
        super(context, attrs);

//        _init(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.carousel_fragment_pager, this,true);

        cTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPageIdDisplay = v.findViewById(R.id.page_indicator);
        mGestureDetector = new GestureDetector(context, new HorizontalSwipeListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(CLASS_TAG, "onInterceptTouchEvent");

        final int action = ev.getAction();

        mGestureDetector.onTouchEvent(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                return false; // pass to child
            }
            case MotionEvent.ACTION_MOVE -> {
                if (mSwipeDetected) return true; // consume. events are dispatched to onTouchEvent
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // when intercept returns true full event stream comes here
        return mGestureDetector.onTouchEvent(ev);
    }

    public void setFragmentManager(@NonNull FragmentManager fMngr) {
        this.mFragmentMngr = fMngr;
    }

    public boolean addFragment(@NonNull Fragment f, final int position) throws IllegalArgumentException {
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
            mFragmentTags.set(position, fTag);
        }

        if (mFragmentTags.size() == 1) {
            mFragmentMngr.beginTransaction().replace(R.id.fragment_container, f, fTag).commit();
        }
        else {
            mFragmentMngr.beginTransaction().add(R.id.fragment_container, f, fTag).hide(f).commit();
        }
        return true;
    }

    public void removeFragment(final int position) throws IllegalArgumentException {
        try {
            final String fTag = mFragmentTags.remove(position);
            mFragmentMngr.beginTransaction().remove(mFragmentMngr.findFragmentByTag(fTag)).commit();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            final String msg = "Trying to remove non-existing fragment: " + e.getClass().getSimpleName();
            throw new IllegalArgumentException(msg);
        }
    }

    private void _switchFragment(int to, int direction) {
        _switchFragment(mCurrentFragment, to, direction);
    }
    private void _switchFragment(int from, int to, int direction) {
        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction();
        switch (direction) {
            case RIGHT_IN -> fTransaction.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left);
            case LEFT_IN -> fTransaction.setCustomAnimations(
                    R.anim.slide_in_left, R.anim.slide_out_right);
            default -> throw new IllegalArgumentException(
                    "Unknown transition direction");
        }
        fTransaction.hide(mFragmentMngr.findFragmentByTag(mFragmentTags.get(from)));
        fTransaction.show(mFragmentMngr.findFragmentByTag(mFragmentTags.get(to)));
        fTransaction.disallowAddToBackStack();

        mCurrentFragment = to;
        _showPageIndicator();

        fTransaction.commit();
    }

    private void _showPageIndicator() {
        mHandler.removeCallbacks(mPageIdAnimation);
        mPageIdDisplay.setText((mCurrentFragment + 1) + "/" + mFragmentTags.size());
        mPageIdDisplay.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mPageIdAnimation, PAGE_ID_DISPLAY_MS);
    }

    private void _init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.carousel_fragment_pager, this,true);

        mPageIdDisplay = v.findViewById(R.id.page_indicator);
//        v.findViewById(R.id.root_view).setOnTouchListener(new ASwipeTouchListener(context) {
//            @Override
//            public void onSwipeLeft() { // == swipe next
//                final int to = (mCurrentFragment == mFragmentTags.size() - 1) ? 0 : mCurrentFragment+1;
//                _switchFragment(to, RIGHT_IN);
//            }
//
//            @Override
//            public void onSwipeRight() {
//                final int to = (mCurrentFragment == 0) ? mFragmentTags.size() - 1 : mCurrentFragment-1;
//                _switchFragment(to, LEFT_IN);
//            }
//        });
    }

    public void onSwipeLeft() { // == swipe next
        Log.d(CLASS_TAG, "onSwipeLeft");
        mSwipeDetected = true;
        final int to = (mCurrentFragment == mFragmentTags.size() - 1) ? 0 : mCurrentFragment+1;
        _switchFragment(to, RIGHT_IN);
    }

    public void onSwipeRight() {
        Log.d(CLASS_TAG, "onSwipeRight");
        mSwipeDetected = true;
        final int to = (mCurrentFragment == 0) ? mFragmentTags.size() - 1 : mCurrentFragment-1;
        _switchFragment(to, LEFT_IN);
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
                final float diffX = Math.abs(e1.getX() - e2.getX());
                final float diffY = Math.abs(e1.getY() - e2.getY());

                if (diffX > cTouchSlop && diffX > diffY) {
                    // This is a horizontal swipe! Parent takes over.
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
            }
            return false;
        }
    }
}
