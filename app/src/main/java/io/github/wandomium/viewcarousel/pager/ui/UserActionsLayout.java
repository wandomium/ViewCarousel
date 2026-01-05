package io.github.wandomium.viewcarousel.pager.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.MainActivity;
import io.github.wandomium.viewcarousel.pager.Settings;

public class UserActionsLayout extends ConstraintLayout implements ICaptureInput
{
    private static final String CLASS_TAG = UserActionsLayout.class.getSimpleName();

    private MainActivity mMainActivity;

    // Top Menu
    private LinearLayout mTopMenu;

    // Swipe gesture detector - previous, next, focus
    private GestureDetector mGestureDetector;
    private SwipeDetectorListener mSwipeDetectorListener;
    private boolean mCaptureInput  = false;

    public UserActionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        try {
            mMainActivity = (MainActivity) context;
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("RootLayout needs to be instantiated from MainActivity");
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.pager_user_actions, this,true);

        ///// PLACEHOLDER
        mTopMenu = v.findViewById(R.id.top_menu);

        ///// SWIPING
        mSwipeDetectorListener = new SwipeDetectorListener(
                ViewConfiguration.get(context).getScaledTouchSlop(), (direction, distance) -> {
            boolean retval = true;
            switch (direction) {
                case SwipeDetectorListener.SWIPE_LEFT -> mMainActivity.nextPage();
                case SwipeDetectorListener.SWIPE_RIGHT -> mMainActivity.previousPage();
                case SwipeDetectorListener.SWIPE_2FINGER_DOWN -> mMainActivity.onMenuBtnClicked(mTopMenu);
                case SwipeDetectorListener.SWIPE_UP -> {
                    if (!mCaptureInput) {
                        mCaptureInput = mMainActivity.setCaptureInput(true); //this one will call us anyway
                    }
                }
                default -> retval = false; // don't consume the event
            }
            return retval;
        });
        mGestureDetector = new GestureDetector(getContext(), mSwipeDetectorListener);
    }

    /* Called when the view is removed from the window hierarchy
     * (e.g., when its hosting Activity/Fragment is destroyed).
     */
    @Override
    public void onDetachedFromWindow() {
        mSwipeDetectorListener = null; // holds implicit reference to mMainActivity
        mGestureDetector = null;
        mMainActivity = null;
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
    private boolean _showTopMenu(final float distance) {
        final int height = mTopMenu.getHeight();
        final boolean retval =  distance < height;
        if (retval) {
            mTopMenu.setTranslationY(distance-height);
        }
        return retval;
    }

    private boolean _hideTopMenu() {
        mTopMenu.setTranslationY(-mTopMenu.getHeight());
        return false;
    }
    /// END OF HACK
    /// ///////////////
}
