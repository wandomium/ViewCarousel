package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.FragmentBase;
import io.github.wandomium.viewcarousel.data.Page;

public class CarouselFragmentPager extends FrameLayout implements ICaptureInput
{
    private static final String CLASS_TAG = CarouselFragmentPager.class.getSimpleName();

    public static final int MAX_VIEWS = 5;

    protected enum Animation { RIGHT_IN, LEFT_IN }

    private final PageIndicator mPageIdDisplay;

    private FragmentManager mFragmentMngr;

    // Fragment list
    private int mCurrentFragmentIdx = 0;
    private int mIdCount = 0;
    private String _createNewTag() {
        return "f_" + (mIdCount++);
    }
    private final ArrayList<String> mFragmentTags = new ArrayList<>(MAX_VIEWS);

    public CarouselFragmentPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.carousel_fragment_pager, this,true);

        mPageIdDisplay = v.findViewById(R.id.page_indicator);
    }

    /* The primary cleanup method. Called when the view is removed from the window hierarchy
     * (e.g., when its hosting Activity/Fragment is destroyed). This is where you should unregister listeners and nullify references.
     */
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setFragmentManager(@NonNull FragmentManager fMngr) {
        this.mFragmentMngr = fMngr;
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
    public boolean setCaptureInput(final boolean captureReq) {
        // fragment can reject capture if unsupported
        return  _getCurrentFragment().setCaptureInput(captureReq);
    }

    ////// FRAGMENT NAVIGATION
    //////
    public int numFragments() {
        return mFragmentTags.size();
    }
    public int currentFragmentIdx() {
        return mCurrentFragmentIdx;
    }
    public void showNextFragment() {
        _switchFragment(_nextFragmentIdx(), Animation.RIGHT_IN, false); //swipe left
    }
    public void showPreviousFragment() {
        _switchFragment(_previousFragmentIdx(), Animation.LEFT_IN, false); // swipe right
    }
    public boolean showFragment(final int position) {
        if (mFragmentMngr.executePendingTransactions()) {
            return false; //state might not be as expected
        }
        Animation animation;
        if (position == 0 && mCurrentFragmentIdx == mFragmentTags.size() - 1) {
            animation = Animation.LEFT_IN;
        }
        else if (position == mFragmentTags.size() - 1 && mCurrentFragmentIdx == 0) {
            animation = Animation.RIGHT_IN;
        }
        else {
            animation = position < mCurrentFragmentIdx ? Animation.LEFT_IN : Animation.RIGHT_IN;
        }

        _switchFragment(position, animation, false);
        return true;
    }

    ////// FRAGMENT MANIPULATION
    //////
    public void addFragment(final int position, @NonNull Fragment f) throws IllegalArgumentException {
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
    }
    public void removeFragment(final int position) throws IllegalArgumentException {
        try {
            if (position == mCurrentFragmentIdx) {
                _switchFragment(_previousFragmentIdx(), Animation.LEFT_IN, true);
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
        Log.d(CLASS_TAG, "Removed, remaining: " + mFragmentTags.toString() + " current=" + mCurrentFragmentIdx);
    }
    public void removeAllFragments() {
        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction();
        List<Fragment> fragmentList = mFragmentMngr.getFragments();

        for (Fragment f : fragmentList) {
            fTransaction.remove(f);
        }
        fTransaction.commitNow();
        mFragmentTags.clear();
        mCurrentFragmentIdx = 0;
    }
    public void replaceFragment(final int position, Fragment fNew) throws IllegalArgumentException {
        try {
            final Fragment fRemove = mFragmentMngr.findFragmentByTag(mFragmentTags.get(position));
            final String fNewTag = _createNewTag();
            FragmentTransaction fTransaction =
                mFragmentMngr.beginTransaction().remove(fRemove).add(R.id.fragment_container, fNew, fNewTag);
            if (position != mCurrentFragmentIdx) {
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

    ////// HELPER METHODS
    private int _previousFragmentIdx() {
        return (mCurrentFragmentIdx == 0) ? mFragmentTags.size() - 1 : mCurrentFragmentIdx - 1;
    }
    private int _nextFragmentIdx() {
        return (mCurrentFragmentIdx == mFragmentTags.size() - 1) ? 0 : mCurrentFragmentIdx + 1;
    }
    private FragmentBase _getCurrentFragment() {
        // O(N) lookup but we have < 10 fragments
        return (FragmentBase) mFragmentMngr.findFragmentByTag(mFragmentTags.get(mCurrentFragmentIdx));
    }
    private void _switchFragment(int to, Animation direction, boolean replaceCurrent) {
        Log.d(CLASS_TAG, "switch: " + mCurrentFragmentIdx + " -> " + to);
        Log.d(CLASS_TAG, mFragmentTags.toString());
        // TODO: pop enter animations
        FragmentTransaction fTransaction = mFragmentMngr.beginTransaction();
        final FragmentBase fFrom = (FragmentBase) Objects.requireNonNull(
                mFragmentMngr.findFragmentByTag(mFragmentTags.get(mCurrentFragmentIdx)));
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
        }
        fFrom.onHide();
        fTransaction.show(fTo);
        fFrom.onShow();
        fTransaction.disallowAddToBackStack();

        int numpPages = mFragmentTags.size();
        mCurrentFragmentIdx = to;
        if (replaceCurrent) {
            numpPages--;
        }
        mPageIdDisplay.showPageIndicator(mCurrentFragmentIdx +1, numpPages);

        // TODO: we probably don't need commitNow here. only when adding new fragments
        fTransaction.commitNow();
    }
}
