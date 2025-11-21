package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class CarouselViewAdapter extends RecyclerView.Adapter<CarouselViewAdapter.ViewHolder> implements IFocusMngr
{
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

//    @FunctionalInterface
//    public interface OnUrlSelected {
//        void onUrlSelected(int position, String value);
//    }

    private final ArrayList<Page> mPages;
    private final View.OnLongClickListener mLongClickListener;
    private boolean mItemFocusOn = false;
    private boolean mBlockInput = false;

    // TODO: Load from config
    public CarouselViewAdapter(ArrayList<Page> pages, View.OnLongClickListener onLongCLickListener) {
        if (pages == null || pages.isEmpty()) {
            // Add a basic page so we are not empty
            this.mPages = new ArrayList<>();
            this.mPages.add(null);
        }
        else {
            this.mPages = pages;
        }

        this.mLongClickListener = onLongCLickListener;
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

    public void onUrlSelected(int position, String value) {
        mPages.set(position, new Page(value));
        notifyItemChanged(position);
    }
    @Override
    public boolean isInFocus() {
        return mItemFocusOn;
    }
    @Override
    public void onReleaseFocus() {
        mItemFocusOn = false;
    }
    @Override
    public void onFocus() {
        if (mBlockInput) {
            Log.d(CLASS_TAG, "Input blocked");
            return;
        }
        mItemFocusOn = true;
        mLongClickListener.onLongClick(null);
    }
    @Override
    public void blockInput(boolean block) {
        Log.d(CLASS_TAG, "blockInput: " + block);
        mBlockInput = block;
    }
    @Override
    public boolean isBlocked() {
        return mBlockInput;
    }
    @Override
    public boolean onLongClick(View v) {
        Log.d(CLASS_TAG, "onLongClick");
        mItemFocusOn = true;
        mLongClickListener.onLongClick(v);
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_page, parent, false);

//        return new ViewHolder(view, (mPages::set));
        return new ViewHolder(view, this);
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

    private static final int VIEW_NEW_PAGE = 1;
    private static final int VIEW_URL = 2;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private static final String CLASS_TAG = ViewHolder.class.getSimpleName();

        ItemWebPage mWebPage;
        int mCurrentView = -1;
        private final CarouselViewAdapter mAdapter;

        public ViewHolder(@NonNull View itemView, CarouselViewAdapter adapter) {
            super(itemView);
            this.mAdapter = adapter;
        }

        public void bind(Page page) {
            if (page != null) {
                _loadWebPage(page.url);
            }
            else if (mCurrentView != VIEW_NEW_PAGE){
//                ViewGroup container = itemView.findViewById(R.id.container);
//                container.removeAllViews();
//                container.addView(mAddUrlBtn);
                mCurrentView = VIEW_NEW_PAGE;
                itemView.findViewById(R.id.btnAddWebView).setOnClickListener(this::_showAddUrlDialog);
            }
        }

        private void _loadWebPage(final String url) {
            if (mWebPage == null) {
                mWebPage = new ItemWebPage(itemView.getContext());
                mWebPage.setup(mAdapter);
            }
            if (mCurrentView != VIEW_URL) {
                ViewGroup container = itemView.findViewById(R.id.container);
                container.removeAllViews();
                container.addView(mWebPage);
                mCurrentView = VIEW_URL;
//                itemView.findViewById(R.id.btnAddWebView).setOnClickListener(null);
            }
            mWebPage.loadUrl(url);
        }

        private void _showAddUrlDialog(View v) {
            EditText input = new EditText(v.getContext());
            input.setHint("Enter page");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Enter URL")
                    .setView(input)
                    .setPositiveButton("OK", (id, l) -> {
                        final String url = input.getText().toString();
                        mAdapter.onUrlSelected(getAbsoluteAdapterPosition(), url);
//                        bind(url);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
