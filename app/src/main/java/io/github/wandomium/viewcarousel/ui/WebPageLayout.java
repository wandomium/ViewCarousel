package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.github.wandomium.viewcarousel.R;

public class WebPageLayout extends SwipeRefreshLayout
{
    private static final String CLASS_TAG = WebPageLayout.class.getSimpleName();
    private IFocusHandler mFocusHandler;
    private GestureDetector mFocusDetector;
    private WebView mWebView;

    public WebPageLayout(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        _init(ctx);
    }
    public WebPageLayout(Context ctx) {
        super(ctx);
        _init(ctx);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mFocusDetector.onTouchEvent(ev); // detect longPress
        super.onInterceptTouchEvent(ev); // allow and handle scroll
        return mFocusHandler != null && !mFocusHandler.isInFocus(); // prevent propagation of events to webview when not in focus
    }

    public void loadUrl(final String url) {
        if (mWebView != null) {
            mWebView.loadUrl(url);
        }
    }

    public void setFocusHandler(IFocusHandler handler) {
        mFocusHandler = handler;
    }

    public void cleanUp() {
        if (mWebView != null) {
            mWebView.setWebViewClient(new WebViewClient());
        }
        mFocusHandler = null;
        mFocusDetector = null;
    }

    private void _init(Context ctx) {
//        LayoutInflater inflater = LayoutInflater.from(ctx);
//        inflater.inflate(R.layout.web_page, this, true);

        // Setup web view
//        mWebView = findViewById(R.id.wv_webview);
        mWebView = new WebView(ctx);
        addView(mWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

//        addView(mWebView, new ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.MATCH_PARENT));

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

        // Capture and release gestures
        mFocusDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent ignored) {
                if (mFocusHandler != null) {
                    mFocusHandler.onLongClick(mWebView);
                }
            }
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                // This is crucial. Returning true tells the GestureDetector to consume
                // the DOWN event and track the rest of the gesture (including long press).
                return true;
            }
        });
    }
}
