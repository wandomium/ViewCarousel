package io.github.wandomium.viewcarousel.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.Page_old;
import io.github.wandomium.viewcarousel.R;

public class CarouselViewAdapter extends RecyclerView.Adapter<CarouselViewAdapter.ViewHolder>
{
    /** @noinspection unused*/
    private static final String CLASS_TAG = CarouselViewAdapter.class.getSimpleName();

    private static final int ITEM_NEW_PAGE = 1;
    private static final int ITEM_WEB_PAGE = 2;
    private static final int ITEM_CALLS_PAGE = 3;

    private final ArrayList<Page_old> mPageOlds;
    private final AFocusMngr mFocusHandler;
    private final PermissionChecker mPerChecker;

    @FunctionalInterface
    public interface PermissionChecker {
        void checkPermission(String[] per);
    }

    public CarouselViewAdapter(ArrayList<Page_old> pages, AFocusMngr focusMngr, PermissionChecker perChecker) {
        if (pages == null || pages.isEmpty()) {
            // Add a default page so we are not empty
            this.mPageOlds = new ArrayList<>();
            this.mPageOlds.add(null);
        }
        else {
            this.mPageOlds = pages;
        }

        this.mFocusHandler = focusMngr;
        this.mPerChecker = perChecker;
    }

    public ArrayList<Page_old> getPages() {
        return mPageOlds;
    }

    // Insert page after current position
    public int insertPage(int position) {
        mPageOlds.add(position + 1, null);
        notifyItemInserted(position + 1);

        return position + 1;
    }

    // remove page at current position
    public int removePage(int position) {
        if (!mPageOlds.isEmpty()) {
            mPageOlds.remove(position);
            if (mPageOlds.isEmpty()) {
                // add a default page if there are none left
                mPageOlds.add(null);
                notifyItemChanged(position);
            }
            else {
                notifyItemRemoved(position);
            }
        }
        return position;
    }

    public void onPageConfigured(Page_old page, int position, int newItemType) {
        mPageOlds.set(position, page);
        notifyItemChanged(position);

        if (newItemType == ITEM_CALLS_PAGE) {
            mPerChecker.checkPermission(new String[]{Manifest.permission.CALL_PHONE});
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mPageOlds.get(position) == null) {
            return ITEM_NEW_PAGE;
        }
        // TODO udpate this
        else if (mPageOlds.get(position).url.startsWith("http")) {
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
                    this::onPageConfigured);
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
        holder.bind(mPageOlds.get(position));
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
        return mPageOlds.size();
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(final Page_old page);
        public void reload() {}
    }

    public static class WebPageViewHolder extends ViewHolder
    {
        public WebPageViewHolder(@NonNull View itemView, AFocusMngr handler) {
            super(itemView);
            ((ItemWebPage)itemView).setFocusHandler(handler);
        }
        @Override
        public void bind(final Page_old page) {
            ((ItemWebPage)itemView).loadUrl(page.url);
        }
        @Override
        public void reload() {
            ((ItemWebPage)itemView).reload();
        }
    }

    public static class CallsPageViewHolder extends ViewHolder
    {
        private static final int[] btnLayouts = {
                R.id.btn_call_contact0, R.id.btn_call_contact1, R.id.btn_call_contact2
        };

        public CallsPageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(final Page_old page) {
            if (page.contacts != null) {
                for (int i = 0; i < page.contacts.size(); i++) {
                    Button btn = itemView.findViewById(btnLayouts[i]);
                    btn.setVisibility(View.VISIBLE);
                    btn.setText(page.contacts.get(i).name());
                    btn.setTag(page.contacts.get(i)); //TODO maybe use id??
                }
            }
        }
    }

    public static class NewPageViewHolder extends ViewHolder
    {
        @FunctionalInterface
        public interface PageConfigureCb {
            void onPageConfigured(Page_old page, int position, int newType);
        }

        private static final String URL_INIT_TEXT = "https://";
        private final PageConfigureCb mPageConfiguredCb;

        public NewPageViewHolder(@NonNull View itemView, PageConfigureCb pageUpdatedCb) {
            super(itemView);
            mPageConfiguredCb = pageUpdatedCb;
            itemView.findViewById(R.id.btn_add_web_page).setOnClickListener(
                (v) -> _showAddWebPageDialog(v.getContext()));
            itemView.findViewById(R.id.btn_add_call_page).setOnClickListener(
                    (v) -> _addCallPage());
        }

        @Override
        public void bind(final Page_old page) {}

        private void _addCallPage() {
            mPageConfiguredCb.onPageConfigured(new Page_old("CALLS", 0), getAbsoluteAdapterPosition(), ITEM_CALLS_PAGE);
        }

        private void _showAddWebPageDialog(Context ctx)
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
            refreshRate.setValue(Page_old.DEFAULT_REFRESH_RATE);

            // create and show dialog
            new AlertDialog.Builder(ctx)
                    .setTitle("Enter URL and refresh rate in minutes")
                    .setView(customView)
                    .setPositiveButton("OK", (id, l) -> {
                        if (urlInput.getText() != null) {
                            String url = urlInput.getText().toString();
                            if (!url.isEmpty() && !url.equals(URL_INIT_TEXT)) {
                                mPageConfiguredCb.onPageConfigured(new Page_old(url, refreshRate.getValue()), getAbsoluteAdapterPosition(), ITEM_WEB_PAGE);
                            }
                        }
                        id.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
