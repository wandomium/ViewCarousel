package io.github.wandomium.viewcarousel.pager;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;

public class FragmentNewPage extends FragmentBase
{
    private static final String URL_INIT_TEXT = "https://";

    private PageConfiguredCb mPageConfiguredCb;

    @FunctionalInterface
    public interface PageConfiguredCb {
        void onPageConfigured(Page page);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        return inflater.inflate(R.layout.item_new_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_add_web_page)
            .setOnClickListener((ignored) -> _showAddWebPageDialog());
        view.findViewById(R.id.btn_add_call_page)
            .setOnClickListener((ignored) ->
                mPageConfiguredCb.onPageConfigured(Page.createContactsPage(null)));
    }

    public void setPageConfigredCb(PageConfiguredCb cb) {
        this.mPageConfiguredCb = cb;
    }


    private void _showAddWebPageDialog()
    {
        // 2. Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.add_web_page_dialog, null);

        // Configure URL text
        TextInputEditText urlInput = customView.findViewById(R.id.url);
        urlInput.setText(URL_INIT_TEXT);

        // Configure the refresh rate selector
        NumberPicker refreshRate = customView.findViewById(R.id.refresh_rate);
        refreshRate.setMinValue(0);
        refreshRate.setMaxValue(100);
        refreshRate.setValue(Page.DEFAULT_REFRESH_RATE_MIN);

        // create and show dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Enter URL and refresh rate in minutes")
                .setView(customView)
                .setPositiveButton("OK", (id, l) -> {
                    if (urlInput.getText() != null) {
                        String url = urlInput.getText().toString();
                        if (!url.isEmpty() && !url.equals(URL_INIT_TEXT)) {
                            mPageConfiguredCb.onPageConfigured(
                                    Page.createWebPage(url, refreshRate.getValue())
                            );
                        }
                    }
                    id.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
