package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class ViewCarousel extends RecyclerView.Adapter<ViewCarousel.ViewHolder>
{
    @FunctionalInterface
    public interface OnUrlSelected {
        void onUrlSelected(int position, String value);
    }

    private final ArrayList<Page> mPages;

    // TODO: Load from config
    public ViewCarousel(ArrayList<Page> pages) {
        if (pages == null || pages.isEmpty()) {
            // Add a basic page so we are not empty
            this.mPages = new ArrayList<>();
            this.mPages.add(null);
        }
        else {
            this.mPages = pages;
        }
    }

    public ArrayList<Page> getPages() {
        return mPages;
    }

    // Insert page after position
    public int insertPage(int position) {
        int realPosition = position % mPages.size() + 1;
        mPages.add(realPosition, null);
        notifyItemInserted(realPosition);

        return position + 1;
    }

    public int removePage(int position) {
        if (!mPages.isEmpty()) {
            int realPosition = position % mPages.size();
            mPages.remove(realPosition);
            if (mPages.isEmpty()) {
                // add a template page if there are none left
                mPages.add(null);
                notifyItemChanged(realPosition);
            }
            else {
                notifyItemRemoved(realPosition);
            }
        }
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_page, parent, false);

//        return new ViewHolder(view, (mPages::set));
        return new ViewHolder(view, ((position, value) -> {
            mPages.set(position, new Page(value));
            notifyItemChanged(position);
        }));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int realPosition = position % mPages.size();
        holder.bind(mPages.get(realPosition));
    }

    @Override
    public int getItemCount() {
//        return mPages.isEmpty() ? 0 : Integer.MAX_VALUE;
        return mPages.size();
    }

    private static final int VIEW_BTN = 1;
    private static final int VIEW_URL = 2;
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        WebView mWebView;
        SwipeRefreshLayout mSwipeRefresh;
        Button mAddUrlBtn;
        int mCurrentView = VIEW_BTN;

        public ViewHolder(@NonNull View itemView, OnUrlSelected listener) {
            super(itemView);
            _creteAddUrlBtn(listener);
        }

        public void bind(Page page) {
            if (page != null) {
                _loadWebPage(page.url);
            }
            else if (mCurrentView != VIEW_BTN) {
                ViewGroup container = itemView.findViewById(R.id.container);
                container.removeAllViews();
                container.addView(mAddUrlBtn);
                mCurrentView = VIEW_BTN;
            }
        }

        private void _loadWebPage(final String url) {
            if (mWebView == null) {
                _createWebView();
            }
            if (mCurrentView != VIEW_URL) {
                ViewGroup container = itemView.findViewById(R.id.container);
                container.removeAllViews();
                container.addView(mSwipeRefresh);
                mCurrentView = VIEW_URL;
            }
            mWebView.loadUrl(url);
        }

        private void _creteAddUrlBtn(OnUrlSelected listener) {
            mAddUrlBtn = itemView.findViewById(R.id.btnAddWebView);
            mAddUrlBtn.setOnClickListener(v -> {
                EditText input = new EditText(v.getContext());
                input.setHint("Enter page");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Enter URL")
                        .setView(input)
                        .setPositiveButton("OK", (id, l) -> {
                            final String url = input.getText().toString();
                            listener.onUrlSelected(getAbsoluteAdapterPosition(), url);
//                        bind(url);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        private void _createWebView() {
            mWebView = new WebView(itemView.getContext());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);

            mSwipeRefresh = new SwipeRefreshLayout(itemView.getContext());
            mSwipeRefresh.addView(mWebView, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

            mSwipeRefresh.setOnRefreshListener(() -> mWebView.reload());

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    mSwipeRefresh.setRefreshing(true);
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    mSwipeRefresh.setRefreshing(false);
                }
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    mSwipeRefresh.setRefreshing(false);
                    super.onReceivedError(view, request, error);
                }
            });
        }
    }
}
