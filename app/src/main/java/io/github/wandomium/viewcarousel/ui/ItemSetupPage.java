package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.google.android.material.textfield.TextInputEditText;

import io.github.wandomium.viewcarousel.Page;
import io.github.wandomium.viewcarousel.R;

public class ItemSetupPage
{
    @FunctionalInterface
    public interface UrlSelectedCb {
        void onUrlSelected(final Page page);
    }

    public static void showAddWebPageDialog(Context ctx, UrlSelectedCb urlSelectedCb)
    {
        // 2. Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View customView = inflater.inflate(R.layout.add_web_page_dialog, null);

        // Configure URL text
        TextInputEditText urlInput = customView.findViewById(R.id.url);
//        urlInput.setHint("Enter page");
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT);

        // Configure the refresh rate selector
        NumberPicker refreshRate = customView.findViewById(R.id.refresh_rate);
        refreshRate.setMinValue(0);
        refreshRate.setMaxValue(100);
        refreshRate.setValue(Page.DEFAULT_REFRESH_RATE_S);

        // create and show dialog
        new AlertDialog.Builder(ctx)
            .setTitle("Enter URL and refresh rate in minutes")
            .setView(customView)
            .setPositiveButton("OK", (id, l) -> {
                if (urlInput.getText() != null) {
                    String url = urlInput.getText().toString();
                    if (!url.isEmpty() && !url.equals("https://")) {
                        urlSelectedCb.onUrlSelected(new Page(url, refreshRate.getValue()));
                    }
                }
                id.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
