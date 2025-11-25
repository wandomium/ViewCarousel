package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class CarouselViewAdapter extends RecyclerView.Adapter<CarouselViewAdapter.ViewHolder>
{
    /** @noinspection unused*/
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

    private static final int ITEM_NEW_PAGE = 1;
    private static final int ITEM_WEB_PAGE = 2;

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

    public void onWebPageItemAdded(int position, Page page) {
        mPages.set(position, page);
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mPages.get(position) == null) {
            return ITEM_NEW_PAGE;
        }
        else {
            return ITEM_WEB_PAGE;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch (viewType) {
            case ITEM_NEW_PAGE -> new NewPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_page, parent, false),
                    this);
            case ITEM_WEB_PAGE -> new WebPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_web_page, parent, false),
                    mFocusHandler);
            default -> throw new RuntimeException("Invalid View Type");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mPages.get(position), position == mActiveItem);
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
        public abstract void bind(final Page page, final boolean isActiveItem);
        public void reload() {};
    }

    public static class WebPageViewHolder extends ViewHolder
    {
        public WebPageViewHolder(@NonNull View itemView, AFocusMngr handler) {
            super(itemView);
            ((ItemWebPage)itemView).setFocusHandler(handler);
        }

        @Override
        public void bind(final Page page, final boolean isActiveItem) {
            ((ItemWebPage)itemView).loadUrl(page.url);
        }
        @Override
        public void reload() {
            ((ItemWebPage)itemView).reload();
        }
    }

    public static class NewPageViewHolder extends ViewHolder implements ItemSetupPage.UrlSelectedCb
    {
        CarouselViewAdapter mAdapter;
        public NewPageViewHolder(@NonNull View itemView, CarouselViewAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
            itemView.findViewById(R.id.btnAddWebView).setOnClickListener(this::onCLick);
        }

        @Override
        public void bind(final Page page, final boolean isActiveItem) {}

        public void onCLick(View v) {
            ItemSetupPage.showAddWebPageDialog(v.getContext(), this);
        }

        @Override
        public void onUrlSelected(Page page) {
            mAdapter.onWebPageItemAdded(getAbsoluteAdapterPosition(), page);
        }
    }
}
