package io.github.wandomium.viewcarousel.pager;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import io.github.wandomium.viewcarousel.pager.ui.WebPage;

public class FragmentWebPage extends FragmentBase
{
    private WebPage mWebPage;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mUrl;

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
        mWebPage = view.findViewById(R.id.web_view);

        mWebPage.getSettings().setJavaScriptEnabled(true);
        mWebPage.getSettings().setDomStorageEnabled(true);

        // connect swipe refresh layout and web view actions
        mSwipeRefresh.setOnRefreshListener( () -> mWebPage.reload() );
        mWebPage.setWebViewClient(new WebViewClient() {
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

        if (mUrl != null) {
            mWebPage.loadUrl(mUrl);
        }
    }

    @Override
    public void captureInput(boolean capture) {
        super.captureInput(capture);
        mWebPage.captureInput(capture);
        mSwipeRefresh.setEnabled(!capture);
    }

    @Override
    public void onShow() {
        super.onShow();
        if (mWebPage.getUrl() == null) { loadUrl(mUrl); }
//        else { mWebPage.reload(); }
    }

    @Override
    public void onDestroy() {
        // stop any activities that could outlive this fragment and keep dangling references
        super.onDestroy();
    }

    public void setUrl(final String url) {
        mUrl = url;
    }

    public void loadUrl(final String url) {
        mWebPage.loadUrl(url);

        if (url.startsWith("https://meteo.arso.gov.si/uploads/meteo/app/inca/m/")) {
            // This fixes swipe to refresh gesture detection on some pages (for ex. INCA - Full-Screen WebGL map)
            // If view is at the top, we say child can't scroll and we get the gesture
            mSwipeRefresh.setOnChildScrollUpCallback((parent, child) -> mWebPage.getScrollY() == 0);
        }
        else {
            mSwipeRefresh.setOnChildScrollUpCallback(null);
        }
    }
}
