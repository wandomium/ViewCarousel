package io.github.wandomium.webviewcarousel.ui;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.wandomium.webviewcarousel.R;

public class ViewCarousel extends RecyclerView.Adapter<ViewCarousel.ViewHolder>
{
    @FunctionalInterface
    public interface OnUrlSelected {
        void onUrlSelected(int position, String value);
    }

    private final ArrayList<String> mPages;

    // TODO: Load from config
    public ViewCarousel(List<String> pages) {
        this.mPages = new ArrayList<>();
        this.mPages.add(null);
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
            mPages.set(position, value);
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
    public static class ViewHolder extends RecyclerView.ViewHolder {
        WebView mWebView;
        Button mAddUrlBtn;
        int mCurrentView = VIEW_BTN;

        public ViewHolder(@NonNull View itemView, OnUrlSelected listener) {
            super(itemView);

            // Load init screen with selector
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

        public void bind(String url) {
            if (url != null) {
                _loadWebPage(url);
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
                mWebView = new WebView(itemView.getContext());
                mWebView.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT));
                mWebView.setWebViewClient(new WebViewClient());
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.getSettings().setDomStorageEnabled(true);
            }
            if (mCurrentView != VIEW_URL) {
                ViewGroup container = itemView.findViewById(R.id.container);
                container.removeAllViews();
                container.addView(mWebView);
                mCurrentView = VIEW_URL;
            }
            mWebView.loadUrl(url);
        }
    }
}
