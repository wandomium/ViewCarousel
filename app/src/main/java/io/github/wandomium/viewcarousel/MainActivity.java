/**
 * This file is part of ViewCarousel.
 * <p>
 * ViewCarousel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * ViewCarousel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ViewCarousel. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.wandomium.viewcarousel;

import android.app.PictureInPictureParams;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.data.ConfigMngr;
import io.github.wandomium.viewcarousel.data.Page;
import io.github.wandomium.viewcarousel.ui.CarouselFragmentPager;
import io.github.wandomium.viewcarousel.ui.DialogConfigurationList;
import io.github.wandomium.viewcarousel.ui.DialogBugReport;
import io.github.wandomium.viewcarousel.ui.ICaptureInput;
import io.github.wandomium.viewcarousel.ui.UserActionsLayout;

public class MainActivity extends AppCompatActivity implements ICaptureInput, FragmentBase.FragmentDataUpdatedCb
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private OnBackPressedCallback mBackPressedCb;
    private CarouselFragmentPager mFPager;

    private UserActionsLayout mUserActionsLayout;

    private boolean mMenuVisible = false;

    private final int NEW_PAGE_IDX_NONE = -1;
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
        findViewById(R.id.menu_btn).setOnClickListener(this::showPopupMenu);
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());

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
        mFPager.removeAllFragments();
        _clearNewPageFragment();
        _loadPages();
        onDatasetUpdated();
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
    public void showPopupMenu(View view) {
        if (mMenuVisible) { return; }

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

            if (id == R.id.action_add_page) { _addNewPageFragment(currentFrIdx + 1, false); }
            else if (id == R.id.action_remove_page)
            {
                Log.d(CLASS_TAG, "Remove: " + currentFrIdx);
                if (currentFrIdx == mFNewPageIdx) { _clearNewPageFragment(); } //TODO: simplify this call
                if (mFPager.numFragments() == 0) { _addNewPageFragment(0, true);} // replace current fragment with new one
                else { mFPager.removeFragment(currentFrIdx); }
                onDatasetUpdated();
            }
            else if (id == R.id.action_enter_pip) { _enterPipMode(); }
            else if (id == R.id.config_show_btns)
            {
                boolean show = !item.isChecked();
                Settings.getInstance(this).setShowBtns(show);
                item.setChecked(show);
                _showBtns(show);
            }
            else if (id == R.id.config_list_configs) { DialogConfigurationList.show(this); }
            else if (id == R.id.bug_report) { DialogBugReport.show(this); }
            //TODO: user manual
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wandomium/ViewCarousel/blob/main/README.md"));
//            startActivity(intent);
            return true;
        });

        mMenuVisible = true;
        menu.show();
    }

    ////////////////
    /// PageUpdatedCb Impl
    @Override
    public void onFragmentDataUpdated(int type, Page page)
    {
        if (type == Page.PAGE_TYPE_UNKNOWN) {
            // new page was just configured
            mFPager.replaceFragment(
                    mFNewPageIdx, FragmentBase.createFragment(mFNewPageIdx, page)
            );
            _clearNewPageFragment();
        }
        onDatasetUpdated();
    }
    public void onDatasetUpdated() {
        ConfigMngr.savePages(this, mFPager.getOrderedData(), Settings.getInstance(this).configFile());
    }

    ////////////////
    /// NEW PAGE FRAGMENT
    private void _addNewPageFragment(int idx, boolean replace) {
        if (mFNewPageIdx != NEW_PAGE_IDX_NONE) {
            Toast.makeText(this, "New page already exists at index: " + mFNewPageIdx, Toast.LENGTH_SHORT).show();
            return;
        }
        final FragmentNewPage fNewPage = (FragmentNewPage) FragmentBase.createFragment(idx, null);
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
        mFNewPageIdx = idx;
    }
    private void _clearNewPageFragment() {
        mFNewPageIdx = NEW_PAGE_IDX_NONE;
    }


    ////////////////
    ////// HELPERS
    private void _loadPages() {
        ArrayList<Page> pages = ConfigMngr.loadPages(this, Settings.getInstance(this).configFile());

        // TODO: detect if config file was manhandled and has too many pages
        for (Page page : pages) {
            final int idx = mFPager.numFragments();
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB, Page.PAGE_TYPE_CONTACTS:
                    FragmentBase fBase = FragmentBase.createFragment(idx, page);
                    mFPager.addFragment(idx, fBase);
                    break;
            }
        }
        if (mFPager.numFragments() == 0) {
            // add new page fragment
            _addNewPageFragment(0, false);
        }
    }
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
}
