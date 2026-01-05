package io.github.wandomium.viewcarousel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.github.wandomium.viewcarousel.data.Page;

public class FragmentWebPage extends FragmentBase
{
    private static final String CLASS_TAG = FragmentWebPage.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 66;
    
    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefresh;
    private View mBlockerView;
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
        mBlockerView = view.findViewById(R.id.web_view_blocker);

        // enable most settings so pages load correctly
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);  // will also detect forms and enable autofill
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true); //allows requests to location

        // it's one for all but no harm in calling it here
        CookieManager.getInstance().setAcceptCookie(true);

        // for location permission
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // retain = true. we want to retain the permission
                    callback.invoke(origin, true, true);
                }
            }
        });

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
        // mWebView.setEnabled(false);
        setCaptureInput(false);

        if (mUrl != null) {
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    public boolean setCaptureInput(boolean captureReq) {
        mBlockerView.setVisibility(captureReq ? View.GONE : View.VISIBLE);
        mWebView.setEnabled(captureReq);
        mSwipeRefresh.setEnabled(!captureReq);

        return captureReq;
    }

    @Override
    public void updateData(Page page) {
        setUrl(page.url);
        setRefreshRate(page.refresh_rate);
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

    public void setRefreshRate(int rate) { mRefreshRate = rate;}
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
