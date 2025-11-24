package io.github.wandomium.viewcarousel.ui;

import android.view.View;

public interface IFocusHandler extends View.OnLongClickListener
{
    void setFocus(boolean focus);
    boolean isInFocus();

    void blockInput(boolean block);
    boolean isBlocked();
}
