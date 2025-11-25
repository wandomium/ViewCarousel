package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class CarouselViewAdapter extends RecyclerView.Adapter<CarouselViewAdapter.ViewHolder>
{
    /** @noinspection unused*/
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

    private static final int ITEM_NEW_PAGE = 1;
    private static final int ITEM_WEB_PAGE = 2;
    private static final int ITEM_CALLS_PAGE = 3;

    private final ArrayList<Page> mPages;
    private final AFocusMngr mFocusHandler;

    private int mActiveItem = 0;

    public CarouselViewAdapter(ArrayList<Page> pages, AFocusMngr focusHandler) {
        if (pages == null || pages.isEmpty()) {
            // Add a default page so we are not empty
            this.mPages = new ArrayList<>();
            this.mPages.add(null);
        }
        else {
            this.mPages = pages;
        }

        this.mFocusHandler = focusHandler;
    }

    public ArrayList<Page> getPages() {
        return mPages;
    }

    // Insert page after current position
    public int insertPage(int position) {
        mPages.add(position + 1, null);
        notifyItemInserted(position + 1);

        return position + 1;
    }

    // remove page at current position
    public int removePage(int position) {
        if (!mPages.isEmpty()) {
            mPages.remove(position);
            if (mPages.isEmpty()) {
                // add a default page if there are none left
                mPages.add(null);
                notifyItemChanged(position);
            }
            else {
                notifyItemRemoved(position);
            }
        }
        return position;
    }

    public void setActiveItem(int position) {
        final int oldItem = mActiveItem;
        mActiveItem = position;
        notifyItemChanged(oldItem);
        notifyItemChanged(mActiveItem);
    }

    public void onWebPageItemAdded(Page page, int position) {
        mPages.set(position, page);
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mPages.get(position) == null) {
            return ITEM_NEW_PAGE;
        }
        // TODO udpate this
        else if (mPages.get(position).url.startsWith("http")) {
            return ITEM_WEB_PAGE;
        }
        else {
            return ITEM_CALLS_PAGE;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch (viewType) {
            case ITEM_NEW_PAGE -> new NewPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_page, parent, false),
                    this::onWebPageItemAdded, this);
            case ITEM_WEB_PAGE -> new WebPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_web_page, parent, false),
                    mFocusHandler);
            case ITEM_CALLS_PAGE -> new CallsPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calls, parent, false));
            default -> throw new RuntimeException("Invalid View Type");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mPages.get(position));
    }

    // Called when item is put in pool for reuse
    // (goes off screen or is removed - called after notifyItemRemoved
    @Override
    public void onViewRecycled(@NonNull CarouselViewAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
// we don't really want to do this. holder could still be reused for another web page
// TODO: still need to implement the cleanup when actually destroyed
//        holder.cleanUp();
    }

//    @Override
//    onViewDetachedFromWindow(), calls onDetachedFromWindow on all views

    @Override
    public int getItemCount() {
        return mPages.size();
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(final Page page);
        public void reload() {}
    }

    public static class WebPageViewHolder extends ViewHolder
    {
        public WebPageViewHolder(@NonNull View itemView, AFocusMngr handler) {
            super(itemView);
            ((ItemWebPage)itemView).setFocusHandler(handler);
        }

        @Override
        public void bind(final Page page) {
            ((ItemWebPage)itemView).loadUrl(page.url);
        }
        @Override
        public void reload() {
            ((ItemWebPage)itemView).reload();
        }
    }

    public static class CallsPageViewHolder extends ViewHolder
    {
        public CallsPageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(final Page page) {
            //TODO
        }
    }

    public static class NewPageViewHolder extends ViewHolder
    {
        @FunctionalInterface
        public interface UrlSelectedCb {
            void onUrlSelected(final Page page, int position);
        }

        private static final String URL_INIT_TEXT = "https://";
        private final UrlSelectedCb mUrlSelectedCb;
        private final CarouselViewAdapter mAdapter;

        public NewPageViewHolder(@NonNull View itemView, UrlSelectedCb urlSelectedCb, CarouselViewAdapter adapter) {
            super(itemView);
            mUrlSelectedCb = urlSelectedCb;
            mAdapter = adapter;
            itemView.findViewById(R.id.btn_add_web_page).setOnClickListener(
                (v) -> _showAddWebPageDialog(v.getContext(), mUrlSelectedCb));
            itemView.findViewById(R.id.btn_add_call_page).setOnClickListener(
                    (v) -> _addCallPage());
        }

        @Override
        public void bind(final Page page) {}

        private void _addCallPage() {
            mAdapter.getPages().set(getAbsoluteAdapterPosition(), new Page("CALLS", 0));
            mAdapter.notifyItemChanged(getAbsoluteAdapterPosition());
        }
        private void _showAddWebPageDialog(Context ctx, UrlSelectedCb urlSelectedCb)
        {
            // 2. Inflate the custom layout
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View customView = inflater.inflate(R.layout.add_web_page_dialog, null);

            // Configure URL text
            TextInputEditText urlInput = customView.findViewById(R.id.url);
            urlInput.setText(URL_INIT_TEXT);

            // Configure the refresh rate selector
            NumberPicker refreshRate = customView.findViewById(R.id.refresh_rate);
            refreshRate.setMinValue(0);
            refreshRate.setMaxValue(100);
            refreshRate.setValue(Page.DEFAULT_REFRESH_RATE);

            // create and show dialog
            new AlertDialog.Builder(ctx)
                    .setTitle("Enter URL and refresh rate in minutes")
                    .setView(customView)
                    .setPositiveButton("OK", (id, l) -> {
                        if (urlInput.getText() != null) {
                            String url = urlInput.getText().toString();
                            if (!url.isEmpty() && !url.equals(URL_INIT_TEXT)) {
                                urlSelectedCb.onUrlSelected(new Page(url, refreshRate.getValue()), getAbsoluteAdapterPosition());
                            }
                        }
                        id.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
