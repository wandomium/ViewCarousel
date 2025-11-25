package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.github.wandomium.viewcarousel.Page;

public class ItemWebPage extends SwipeRefreshLayout
{
    /** @noinspection unused */
    private static final String CLASS_TAG = ItemWebPage.class.getSimpleName();

    private WebView mWebView;
    private String mUrl;

    private AFocusMngr mFocusMngr;
    private GestureDetector mGestureDetector;

    public ItemWebPage(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        _init(ctx);
    }
    public ItemWebPage(Context ctx) {
        super(ctx);
        _init(ctx);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Swipe refresh, ignore other gestures
        if (super.onInterceptTouchEvent(ev) || isRefreshing()) {
            return true;
        }
        // detect longPress
        mGestureDetector.onTouchEvent(ev);
        // prevent propagation of events to webview when not in focus
        return mFocusMngr != null && !mFocusMngr.isInFocus();
    }

    public void loadUrl(final String url) {
        mUrl = url;
        mWebView.loadUrl(mUrl);
    }
    public void reload() {
        mWebView.reload();
    }

    public void setFocusHandler(AFocusMngr handler) {
        mFocusMngr = handler;
    }

    private void _init(Context ctx) {
        mWebView = new WebView(ctx);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        // !!! Needs constraint layout with fill parent. Some pages do not display properly otherwise
        addView(mWebView, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        // connect swipe refresh layout and web view actions
        setOnRefreshListener( () -> mWebView.reload() );
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isRefreshing()) { setRefreshing(true); }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isRefreshing()) { setRefreshing(false); }
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (isRefreshing()) { setRefreshing(false); }
                super.onReceivedError(view, request, error);
            }
        });

        // Detect LONG PRESS for CAPTURE
        mGestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent ignored) {
                // Do not activate if Refreshing -> leads to loads of spurious captures
                if (!isRefreshing() && mFocusMngr != null) {
                    mFocusMngr.onLongClick(mWebView);
                }
            }
            @Override
            public boolean onDown(@NonNull MotionEvent ignored) {
                // This is crucial. Returning true tells the GestureDetector to consume
                // the DOWN event and track the rest of the gesture (including long press).
                return true;
            }
        });
    }
}
