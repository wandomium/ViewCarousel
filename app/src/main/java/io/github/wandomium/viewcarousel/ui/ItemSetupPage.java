package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import io.github.wandomium.viewcarousel.R;

public class ItemSetupPage extends FrameLayout
{
    @FunctionalInterface
    public interface UrlSelectedCb {
        void onUrlSelected(final String url);
    }

    private UrlSelectedCb mUrlSelectedCb;
    private Button mAddWepPageIitemBtn;

    public ItemSetupPage(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        _init(ctx);
    }
    public ItemSetupPage(Context ctx) {
        super(ctx);
        _init(ctx);
    }

    public void setUrlSelectedCb(UrlSelectedCb cb) {
        this.mUrlSelectedCb = cb;
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
//                    mAdapter.onWebPageAdded(getAbsoluteAdapterPosition(), url);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void _init(Context ctx) {
        mAddWepPageIitemBtn = findViewById(R.id.btn_add_web_page_item);
    }
}
