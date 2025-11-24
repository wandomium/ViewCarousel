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
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

    private static final int ITEM_NEW_PAGE = 1;
    private static final int ITEM_WEB_PAGE = 2;

    private final ArrayList<Page> mPages;
    private final AFocusHandler mFocusHandler;

    public CarouselViewAdapter(ArrayList<Page> pages, AFocusHandler focusHandler) {
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

    public void onWebPageItemAdded(int position, String value) {
        mPages.set(position, new Page(value));
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
                    this::onWebPageItemAdded);
            case ITEM_WEB_PAGE -> new WebPageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_web_page, parent, false),
                    mFocusHandler);
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
        public abstract void cleanUp();
    }

    public static class WebPageViewHolder extends ViewHolder
    {
        public WebPageViewHolder(@NonNull View itemView, AFocusHandler handler) {
            super(itemView);
            ((ItemWebPage)itemView).setFocusHandler(handler);
        }

        @Override
        public void bind(final Page page) {
            ((ItemWebPage)itemView).loadUrl(page.url);
        }
        @Override
        public void cleanUp() {}
    }

    public static class NewPageViewHolder extends ViewHolder
    {
        @FunctionalInterface
        public interface UrlSelectedCb {
            void onUrlSelected(final int position, final String url);
        }
        private UrlSelectedCb mUrlSelectedCb;

        public NewPageViewHolder(@NonNull View itemView, UrlSelectedCb urlSelectedCb) {
            super(itemView);
            this.mUrlSelectedCb = urlSelectedCb;
            itemView.findViewById(R.id.btnAddWebView).setOnClickListener(this::_showAddUrlDialog);
        }

        @Override
        public void bind(Page page) {}
        @Override
        public void cleanUp() {
            itemView.findViewById(R.id.btnAddWebView).setOnClickListener(null);
            mUrlSelectedCb = null;
        }

        private void _showAddUrlDialog(View v) {
            final int itemPosition = getAbsoluteAdapterPosition();
            EditText input = new EditText(v.getContext());
            input.setHint("Enter page");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Enter URL")
                    .setView(input)
                    .setPositiveButton("OK", (id, l) -> {
                        final String url = input.getText().toString();
                        mUrlSelectedCb.onUrlSelected(itemPosition, url);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
