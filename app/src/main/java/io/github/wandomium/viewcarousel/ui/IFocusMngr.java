package io.github.wandomium.viewcarousel.ui;

import android.view.View;

public interface IFocusMngr extends View.OnLongClickListener
{
    boolean isInFocus();
    void blockInput(boolean block);
    boolean isBlocked();
    void onFocus();
    void onReleaseFocus();

    @Override
    default boolean onLongClick(View v) {
        if (!isInFocus()) {
            onFocus();
        }
        return false; // propagate event
    }
}
