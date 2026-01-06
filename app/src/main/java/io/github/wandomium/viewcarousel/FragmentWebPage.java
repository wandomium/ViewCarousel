/**
 * This file is part of ViewCarousel.
 * <p>
 * ViewCarousel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * ViewCarousel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ViewCarousel. If not, see <https://www.gnu.org/licenses/>.
 */
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
    @SuppressWarnings("unused")
    private static final String CLASS_TAG = FragmentWebPage.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 66;
    
    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefresh;
    private View mBlockerView;

    private static final long TIME_UNITS = 1000L * 60L;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPage.refresh_rate > 0) {
                mWebView.reload();
                mHandler.postDelayed(this, mPage.refresh_rate * TIME_UNITS);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPage == null) {
            mPage = Page.createWebPage("", 0);
        }
    }

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
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
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

        if (!mPage.url.isEmpty()) {
            mWebView.loadUrl(mPage.url);
        }
    }

    @Override
    public void onDestroyView() {
        // nullify all view references
        mWebView = null;
        mSwipeRefresh = null;
        mBlockerView = null;

        mHandler.removeCallbacks(mRefreshRunnable);

        super.onDestroyView();
    }

    @Override
    public boolean setCaptureInput(boolean captureReq) {
        mBlockerView.setVisibility(captureReq ? View.GONE : View.VISIBLE);
        mWebView.setEnabled(captureReq);
        mSwipeRefresh.setEnabled(!captureReq);

        return captureReq;
    }

    @Override
    public void onShow() {
        super.onShow();
        if (mWebView.getUrl() == null && mPage != null) { loadUrl(mPage.url); }
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

    public void loadUrl(final String url) {
        if (!mPage.url.equals(url)) {
            mPage = Page.createWebPage(url, mPage.refresh_rate);
            mPageUpdatedCb.onFragmentDataUpdated(Page.PAGE_TYPE_WEB, mPage);
        }
        mWebView.loadUrl(mPage.url);

        if (mPage.url.startsWith("https://meteo.arso.gov.si/uploads/meteo/app/inca/m/")) {
            // This fixes swipe to refresh gesture detection on some pages (for ex. INCA - Full-Screen WebGL map)
            // If view is at the top, we say child can't scroll and we get the gesture
            mSwipeRefresh.setOnChildScrollUpCallback((parent, child) -> mWebView.getScrollY() == 0);
        }
        else {
            mSwipeRefresh.setOnChildScrollUpCallback(null);
        }
    }
}
