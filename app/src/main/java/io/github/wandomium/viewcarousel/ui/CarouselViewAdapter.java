package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class CarouselViewAdapter extends RecyclerView.Adapter<CarouselViewAdapter.ViewHolder> implements IFocusHandler
{
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

    private static final int VIEW_NEW_PAGE = 1;
    private static final int VIEW_URL = 2;

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
        mPages.add(position + 1, null);
        notifyItemInserted(position + 1);

        return position + 1;
    }

    public int removePage(int position) {
        if (!mPages.isEmpty()) {
            mPages.remove(position);
            if (mPages.isEmpty()) {
                // add a template page if there are none left
                mPages.add(null);
                notifyItemChanged(position);
            }
            else {
                notifyItemRemoved(position);
            }
        }
        return position;
    }

    public void onWebPageAdded(int position, String value) {
        mPages.set(position, new Page(value));
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mPages.get(position) == null) {
            return VIEW_NEW_PAGE;
        }
        else {
            return VIEW_URL;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = switch (viewType) {
            case VIEW_NEW_PAGE -> LayoutInflater.from(parent.getContext()).inflate(R.layout.new_page, parent, false);
            case VIEW_URL -> LayoutInflater.from(parent.getContext()).inflate(R.layout.web_page, parent, false);
            default -> throw new RuntimeException("Invalid View Type");
        };

        return ViewHolder._createViewHolder(itemView, this, viewType);
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

    @Override
    public boolean isInFocus() {
        return mItemFocusOn;
    }
    @Override
    public void setFocus(boolean focus) {
        mItemFocusOn = focus;
    }
    @Override
    public boolean onLongClick(View v) {
        return mBlockInput ? false : mLongClickListener.onLongClick(v);
    }
    @Override
    public void blockInput(boolean block) {
        mBlockInput = block;
    }
    @Override
    public boolean isBlocked() {
        return mBlockInput;
    }


    public static abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public static ViewHolder _createViewHolder(@NonNull View itemView, CarouselViewAdapter adapter, int itemType) {
            return switch (itemType) {
                case VIEW_NEW_PAGE -> new NewPageViewHolder(itemView, adapter);
                case VIEW_URL -> new WebPageViewHolder(itemView, adapter);
                default -> throw new RuntimeException("Invalid View Type");
            };
        }
        public abstract void bind(final Page page);
        public abstract void cleanUp();
    }

    public static class WebPageViewHolder extends ViewHolder
    {
        public WebPageViewHolder(@NonNull View itemView, CarouselViewAdapter adapter) {
            super(itemView);
            ((WebPageLayout)itemView).setFocusHandler(adapter);
        }

        @Override
        public void bind(final Page page) {
            ((WebPageLayout)itemView).loadUrl(page.url);
        }
        @Override
        public void cleanUp() {
            ((WebPageLayout)itemView).cleanUp();
        }
    }

    public static class NewPageViewHolder extends ViewHolder
    {
        private CarouselViewAdapter mAdapter;
        public NewPageViewHolder(@NonNull View itemView, CarouselViewAdapter adapter) {
            super(itemView);
            this.mAdapter = adapter;
            itemView.findViewById(R.id.btnAddWebView).setOnClickListener(this::_showAddUrlDialog);
        }
        @Override
        public void bind(Page page) {}
        @Override
        public void cleanUp() {
            itemView.findViewById(R.id.btnAddWebView).setOnClickListener(null);
            mAdapter = null;
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
                        mAdapter.onWebPageAdded(getAbsoluteAdapterPosition(), url);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
