package io.github.wandomium.viewcarousel.pager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;

public class FragmentWebPage extends FragmentBase
{
    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mUrl;

    private static final long TIME_UNITS = 1000L * 60L;
    private int mRefreshRate = 0;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRefreshRate > 0) {
                mWebView.reload();
                mHandler.postDelayed(this, mRefreshRate * TIME_UNITS);
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        return inflater.inflate(R.layout.fragment_web_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        mWebView = view.findViewById(R.id.web_view);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        // connect swipe refresh layout and web view actions
        mSwipeRefresh.setOnRefreshListener( () -> mWebView.reload() );
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!mSwipeRefresh.isRefreshing()) { mSwipeRefresh.setRefreshing(true); }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mSwipeRefresh.isRefreshing()) { mSwipeRefresh.setRefreshing(false); }
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (mSwipeRefresh.isRefreshing()) { mSwipeRefresh.setRefreshing(false); }
                super.onReceivedError(view, request, error);
            }
        });

        // default capture settings
        mWebView.setEnabled(false);

        if (mUrl != null) {
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    public void captureInput(boolean capture) {
        super.captureInput(capture);
        mWebView.setEnabled(capture);
        mSwipeRefresh.setEnabled(!capture);
    }

    @Override
    public void onShow() {
        super.onShow();
        if (mWebView.getUrl() == null) { loadUrl(mUrl); }
        else {
            mHandler.post(mRefreshRunnable);
        }
//        else { mWebPage.reload(); }
    }

    @Override
    public void onHide() {
        super.onHide();
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    @Override
    public void onDestroy() {
        // stop any activities that could outlive this fragment and keep dangling references
        mHandler.removeCallbacks(mRefreshRunnable);
        super.onDestroy();
    }

    public void setmRefreshRate(int rate) { mRefreshRate = rate;}
    public void setUrl(final String url) { mUrl = url; }

    public void loadUrl(final String url) {
        mWebView.loadUrl(url);

        if (url.startsWith("https://meteo.arso.gov.si/uploads/meteo/app/inca/m/")) {
            // This fixes swipe to refresh gesture detection on some pages (for ex. INCA - Full-Screen WebGL map)
            // If view is at the top, we say child can't scroll and we get the gesture
            mSwipeRefresh.setOnChildScrollUpCallback((parent, child) -> mWebView.getScrollY() == 0);
        }
        else {
            mSwipeRefresh.setOnChildScrollUpCallback(null);
        }
    }
}
