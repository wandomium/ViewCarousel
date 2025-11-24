package io.github.wandomium.viewcarousel.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ItemWebPage extends SwipeRefreshLayout
{
    private static final String CLASS_TAG = ItemWebPage.class.getSimpleName();
    private AFocusHandler mFocusHandler;
    private GestureDetector mGestureDetector;
    private WebView mWebView;

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
        if (super.onInterceptTouchEvent(ev)) {
            // this was a refresh gesture
            Log.d(CLASS_TAG, "Swipe refresh detected");
            mFocusHandler.blockInput(true);
            return true;
        }
        mGestureDetector.onTouchEvent(ev); // detect longPress
        return mFocusHandler != null && !mFocusHandler.isInFocus(); // prevent propagation of events to webview when not in focus
    }

    public void loadUrl(final String url) {
        if (mWebView != null) {
            mWebView.loadUrl(url);
        }
    }

    public void setFocusHandler(AFocusHandler handler) {
        mFocusHandler = handler;
    }

    public void cleanUp() {
        if (mWebView != null) {
            mWebView.setWebViewClient(new WebViewClient());
        }
        mFocusHandler = null;
        mGestureDetector = null;
    }

    private void _init(Context ctx) {
//        LayoutInflater inflater = LayoutInflater.from(ctx);
//        inflater.inflate(R.layout.web_page, this, true);

        // Setup web view
//        mWebView = findViewById(R.id.wv_webview);
        mWebView = new WebView(ctx);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        // !!! Needs constraint layout. Some pages do not display properly otherwise
        //        addView(mWebView);
        addView(mWebView, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

        // connect swipe refresh and web view actions
        setOnRefreshListener( () -> mWebView.reload() );
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isRefreshing()) {
                    setRefreshing(true);
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isRefreshing()) {
                    setRefreshing(false);
                }
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (isRefreshing()) {
                    setRefreshing(false);
                }
                super.onReceivedError(view, request, error);
            }
        });

        // Capture and release gestures
        mGestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent ignored) {
                Log.d(CLASS_TAG, "Long press detected");
                if (mFocusHandler != null) {
                    mFocusHandler.onLongClick(mWebView);
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
