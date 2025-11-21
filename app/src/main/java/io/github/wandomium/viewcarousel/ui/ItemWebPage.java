package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ItemWebPage extends SwipeRefreshLayout
{
    private static final String CLASS_TAG = ItemWebPage.class.getSimpleName();
    private IFocusMngr mFocusMngr;
    private GestureDetector mGestureDetector;
    private WebView mWebView;

    public ItemWebPage(Context ctx) {
        super(ctx);

        mGestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
            private boolean mScrolling = false;
            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(CLASS_TAG, "longpress to " + ViewConfiguration.getLongPressTimeout() + " scrolling " + mScrolling);
                if (mFocusMngr != null && !mScrolling) {
                    mFocusMngr.onFocus();
                }
            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Reset the flag on tap up/end of a touch sequence
                mScrolling = false;
                // Execute single tap logic
                return super.onSingleTapUp(e);
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Fling is detected.
                mScrolling = true; // Flings also imply movement, so keep the flag true.

                // Execute fling logic here...
                return true;
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Fling is detected.
                mScrolling = true; // Flings also imply movement, so keep the flag true.

                // Execute fling logic here...
                return true;
            }
            @Override
            public boolean onDown(MotionEvent e) {
                // This is crucial. Returning true tells the GestureDetector to consume
                // the DOWN event and track the rest of the gesture (including long press).
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(CLASS_TAG, "onInterceptTouchEvent, retval " + (mFocusMngr != null && !mFocusMngr.isInFocus()));
        mGestureDetector.onTouchEvent(ev);
        super.onInterceptTouchEvent(ev); // allow and handle scroll
        return mFocusMngr != null && !mFocusMngr.isInFocus(); // prevent propagation of events to webview when not in focus
    }

    public void loadUrl(final String url) {
        if (mWebView != null) {
            mWebView.loadUrl(url);
        }
    }

    public void setup(IFocusMngr focusMngr) {
        mFocusMngr = focusMngr;

        // Add web view to layout
        mWebView = new WebView(getContext());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        addView(mWebView, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

        // connect swipe refresh and web view actions
        setOnRefreshListener(() -> mWebView.reload());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setRefreshing(true);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                setRefreshing(false);
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                setRefreshing(false);
                super.onReceivedError(view, request, error);
            }
        });

        // add gesture blocker, so web view only moves when CAPTURE is active
//        mWebView.setLongClickable(true);
//        mWebView.setOnLongClickListener(mFocusMngr);
    }

    public void cleanUp() {
        if (mWebView != null) {
            mWebView.setOnLongClickListener(null);
            mWebView.setWebViewClient(new WebViewClient());
            mWebView = null;
        }
        mFocusMngr = null;
        mGestureDetector = null;
    }
}
