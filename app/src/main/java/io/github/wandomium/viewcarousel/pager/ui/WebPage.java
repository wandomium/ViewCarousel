package io.github.wandomium.viewcarousel.pager.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.annotation.NonNull;

public class WebPage extends WebView
{
    private boolean mInputEnabled = false;

    public WebPage(@NonNull Context context) {
        super(context);
    }
    public WebPage(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mInputEnabled ? super.onTouchEvent(ev) : true;
    }

    public void captureInput(final boolean enable) {
        mInputEnabled = enable;
    }
}
