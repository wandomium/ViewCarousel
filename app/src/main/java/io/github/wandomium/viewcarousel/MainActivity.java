package io.github.wandomium.viewcarousel;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Rational;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.data.ConfigMngr;
import io.github.wandomium.viewcarousel.data.Page;
import io.github.wandomium.viewcarousel.ui.CarouselFragmentPager;
import io.github.wandomium.viewcarousel.ui.ConfigurationListDialog;
import io.github.wandomium.viewcarousel.ui.ICaptureInput;
import io.github.wandomium.viewcarousel.ui.UserActionsLayout;

public class MainActivity extends AppCompatActivity implements ICaptureInput
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private OnBackPressedCallback mBackPressedCb;
    private CarouselFragmentPager mFPager;

    private UserActionsLayout mUserActionsLayout;

    private boolean mMenuVisible = false;

    private final int NEW_PAGE_IDX_NONE = -1;
    private FragmentNewPage mFNewPage = null;
    private int mFNewPageIdx = NEW_PAGE_IDX_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// fragment pager
        mFPager = findViewById(R.id.carousel_pager);
        mFPager.setFragmentManager(getSupportFragmentManager());
        _loadPages();

        /// SWIPES
        mUserActionsLayout = findViewById(R.id.user_actions_view);

        /// BUTTONS
        findViewById(R.id.call_btn).setOnClickListener(this::onCallBtnClicked);
        findViewById(R.id.menu_btn).setOnClickListener(this::onMenuBtnClicked);

        /// release focus
        mBackPressedCb = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                setCaptureInput(false);
            }
        };
        getOnBackPressedDispatcher().addCallback(mBackPressedCb);

        /// this is for all webviews
        CookieManager.getInstance().setAcceptCookie(true);
    }

    @Override
    protected void onPause() {
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() { // auto enter pip mode
        super.onUserLeaveHint();
        _enterPipMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
        _showBtns(!isInPipMode);
    }

    ////// ICaptureInput IMPL
    @Override
    public boolean setCaptureInput(final boolean captureReq) {
        // If current fragment does not support capture it can reject it
        final boolean capture = mFPager.setCaptureInput(captureReq);
        mBackPressedCb.setEnabled(capture);
        mUserActionsLayout.setCaptureInput(capture);
        _showBtns(!capture);

        //if capture was requested but not realized we don't write
        //release is currently never rejected
        //TODO it would be cleaner to have two methods here but later...
        if (!(captureReq && !capture)) {
            Toast.makeText(MainActivity.this, capture ? "CAPTURE" : "RELEASE", Toast.LENGTH_SHORT).show();
        }
        return capture;
    }

    ////////////////
    /////// PAGE MANIPULATION
    public void nextPage() { mFPager.showNextFragment(); }
    public void previousPage() { mFPager.showPreviousFragment(); }
    public void reloadPagesFromConfig() {
        // clear all pages
        mFPager.removeAllFragments();
        _removeNewPageFragment();
        // load freash from config
        _loadPages();
    }

    ////////////////
    /////// BUTTONS
    public void onCallBtnClicked(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Could not launch dialer", Toast.LENGTH_LONG).show();
        }
    }
    public void onMenuBtnClicked(View view) {
        if (mMenuVisible) {
            return;
        }
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());

        // show btns menu item
        MenuItem showBtnsItem = menu.getMenu().findItem(R.id.config_show_btns);
        showBtnsItem.setChecked(Settings.getInstance(this).showBtns());

        // action listeners
        menu.setOnDismissListener((ignored) -> mMenuVisible = false);
        menu.setOnMenuItemClickListener((item) -> {
            final int id = item.getItemId();
            final int currentFrIdx = mFPager.currentFragmentIdx();

            if (id == R.id.action_add_page)
            {
                _addNewPageFragment(currentFrIdx + 1, false);
            }
            else if (id == R.id.action_remove_page)
            {
                Log.d(CLASS_TAG, "Remove: " + currentFrIdx);
                if (currentFrIdx == mFNewPageIdx) { _removeNewPageFragment(); } //TODO: simplify this call
                if (mFPager.numFragments() == 0) { _addNewPageFragment(0, true);} // replace current fragment with new one
                else { mFPager.removeFragment(currentFrIdx); }
                onDatasetUpdated();
            }
            else if (id == R.id.action_enter_pip)
            {
                _enterPipMode();
            }
            else if (id == R.id.config_show_btns)
            {
                boolean show = !item.isChecked();
                Settings.getInstance(this).setShowBtns(show);
                item.setChecked(show);
                _showBtns(show);
            }
            else if (id == R.id.config_list_configs)
            {
                ConfigurationListDialog.show(this);
            }
            return true;
        });

        mMenuVisible = true;
        menu.show();
    }

    ////////////////
    /// PageUpdatedCb Impl
    public void onDatasetUpdated(int fIdx, Page page) { onDatasetUpdated();}
    public void onDatasetUpdated() {
        ArrayList<Page> pages = new ArrayList<>();
        for (Fragment fBase : getSupportFragmentManager().getFragments() ) {
            pages.add(((FragmentBase)fBase).getData());
        }

        ConfigMngr.savePages(this, pages, Settings.getInstance(this).configFile());
    }
    public void onNewPageConfigured(int fIdx, Page page) {
        if (fIdx != mFNewPageIdx) {
            // TODO check this, should not happen
            Log.e(CLASS_TAG, "newPageConfigured fragment idx does not match new page idx " + fIdx + " != " + mFNewPageIdx);
        }
        FragmentBase fBase = null;
        switch (page.page_type) {
            case Page.PAGE_TYPE_CONTACTS:
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 333);
            case Page.PAGE_TYPE_WEB:
                fBase = FragmentBase.createFragment(mFNewPageIdx, page, MainActivity.this::onDatasetUpdated);
                break;
        }
        if (fBase != null) {
            mFPager.replaceFragment(mFNewPageIdx, fBase);
            _removeNewPageFragment();
        }
    }

    ////////////////
    ////// HELPERS
    private void _showBtns(final boolean showRequest) {
        final boolean show = showRequest && Settings.getInstance(this).showBtns();

        findViewById(R.id.menu_btn).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.call_btn).setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void _enterPipMode() {
        Rational ratio = new Rational(9, 12);
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(ratio)
                .setSeamlessResizeEnabled(true)
                .build();
        enterPictureInPictureMode(params);
    }
    private void _loadPages() {
        ArrayList<Page> pages = ConfigMngr.loadPages(this, Settings.getInstance(this).configFile());

        // TODO: detect if config file was manhandled and has too many pages
        for (Page page : pages) {
            final int idx = mFPager.numFragments();
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB, Page.PAGE_TYPE_CONTACTS:
                    FragmentBase fBase = FragmentBase.createFragment(idx, page, this::onDatasetUpdated);
                    mFPager.addFragment(idx, fBase);
                    break;
            }
        }
        if (mFPager.numFragments() == 0) {
            // add new page fragment
            _addNewPageFragment(0, false);
        }
    }

    ////////////////
    /// NEW PAGE FRAGMENT
    private void _addNewPageFragment(int idx, boolean replace) {
        if (mFNewPageIdx != NEW_PAGE_IDX_NONE) {
            Toast.makeText(this, "New page already exists at index: " + mFNewPageIdx, Toast.LENGTH_SHORT).show();
            return;
        }
        final FragmentNewPage fNewPage = (FragmentNewPage) FragmentBase.createFragment(idx, null, this::onNewPageConfigured);
        try {
            if (replace) {
                mFPager.replaceFragment(idx, fNewPage);
            }
            else {
                mFPager.addFragment(idx, fNewPage);
                mFPager.showFragment(idx);
            }
        } catch (IllegalArgumentException ignored) {
            Toast.makeText(MainActivity.this, "Max page limit reached", Toast.LENGTH_LONG).show();
            return;
        }
        mFNewPage = fNewPage;
        mFNewPageIdx = idx;
    }
    private void _removeNewPageFragment() {
        if (mFNewPage != null) {
            mFNewPage.setPageUpdatedCb(null);
        }
        mFNewPage = null;
        mFNewPageIdx = NEW_PAGE_IDX_NONE;
    }

    ////////////////
    ////////////////
    ////////////////
    private void _showUnsupportedActionToast() {
        Toast.makeText(this, "Unsupported action", Toast.LENGTH_LONG).show();
    }

    private void _loadDummyPages() {
        // temp
        int i = 0;
        for (; i < CarouselFragmentPager.MAX_VIEWS - 1; i++) {
            mFPager.addFragment(i,
                    FragmentBase.createFragment(i, null, this::onNewPageConfigured));
        }
        mFPager.addFragment(i++,
            FragmentBase.createFragment(i, Page.createWebPage("https://archlinux.org", 1), this::onDatasetUpdated));
    }
}
