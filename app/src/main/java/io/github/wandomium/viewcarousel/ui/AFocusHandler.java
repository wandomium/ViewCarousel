package io.github.wandomium.viewcarousel.ui;

import android.view.View;

import androidx.activity.OnBackPressedCallback;

/* Handle CAPTURE and RELEASE on view */
public abstract class AFocusHandler extends OnBackPressedCallback implements View.OnLongClickListener
{
    /* TO IMPLEMENT */
    protected abstract boolean _onObtainFocus(View v);
    protected abstract void    _onReleaseFocus();

    /* CLASS LOGIC */
    public AFocusHandler() { super(true); }

    private boolean mItemFocusOn = false;
    private boolean mBlockInput = false;

    public boolean isInFocus() { return mItemFocusOn; }
    public boolean isBlocked() { return mBlockInput; }
    public void blockInput(boolean block) { mBlockInput = block; }

    /* back press is used to release focus from the ITEM */
    @Override
    public void handleOnBackPressed() {
        if (mItemFocusOn) {
            mItemFocusOn = false;
            _onReleaseFocus();
        }
    }
    /* long click is used to capture focus on the ITEM */
    @Override
    public boolean onLongClick(View v) {
        // if input is blocked ignore click ??? TODO is this ok???? maybe consume and not send to children - test it out
        if (mBlockInput) { return false; }
        // if item was in focus return false and precess any events in child
        if (mItemFocusOn) { return false; }
        // if item was not in focus then call _onGetFocus
        else {
            mItemFocusOn = true;
            return _onObtainFocus(v);
        }
    }
}
