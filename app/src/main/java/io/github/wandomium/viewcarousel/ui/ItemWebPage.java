package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.graphics.Bitmap;
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

import java.util.Objects;

public class ItemWebPage extends SwipeRefreshLayout
{
    /** @noinspection unused */
    private static final String CLASS_TAG = ItemWebPage.class.getSimpleName();

    private WebView mWebView;

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
            if (!mFocusMngr.isInFocus()) {
                // handle refresh
                return true;
            }
        }
        // detect longPress
        mGestureDetector.onTouchEvent(ev);
        // prevent propagation of events to webview when not in focus
        return !mFocusMngr.isInFocus();
    }

    public void loadUrl(final String url) {
        Objects.requireNonNull(mFocusMngr); //called on bind. might as well test it here
        mWebView.loadUrl(url);

        if (url.startsWith("https://meteo.arso.gov.si/uploads/meteo/app/inca/m/")) {
            // This fixes swipe to refresh gesture detection on some pages (for ex. INCA - Full-Screen WebGL map)
            // If view is at the top, we say child can't scroll and we get the gesture
            setOnChildScrollUpCallback((parent, child) -> mWebView.getScrollY() == 0);
        }
        else {
            setOnChildScrollUpCallback(null);
        }
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
        setOnRefreshListener( () -> {
            if (mFocusMngr.isInFocus()) { setRefreshing(false);}
            else { mWebView.reload(); }
        } );
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
