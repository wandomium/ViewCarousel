package io.github.wandomium.viewcarousel.pager;

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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.ConfigMngr;
import io.github.wandomium.viewcarousel.pager.data.Page;
import io.github.wandomium.viewcarousel.pager.ui.CarouselFragmentPager;
import io.github.wandomium.viewcarousel.pager.ui.ConfigurationListDialog;
import io.github.wandomium.viewcarousel.pager.ui.ICaptureInput;
import io.github.wandomium.viewcarousel.pager.ui.UserActionsLayout;

public class MainActivity extends AppCompatActivity implements ICaptureInput
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private OnBackPressedCallback mBackPressedCb;
    private CarouselFragmentPager mFPager;

    // TODO: have data stored in fragments and call them on save - single place for data store
    private ArrayList<Page> mPages;

    private UserActionsLayout mUserActionsLayout;

    private boolean mMenuVisible = false;

    private final int NEW_PAGE_IDX_NONE = -1;
    private FragmentNewPage mFNewPage = null;
    private int mFNewPageIdx = NEW_PAGE_IDX_NONE;

    private ActivityResultLauncher<Intent> mContactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity_main);

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

        /// contact select
        mContactPickerLauncher = _createContactPickerLauncher();

        /// this is for all webviews
        CookieManager.getInstance().setAcceptCookie(true);
    }

    @Override
    protected void onPause() {
        ConfigMngr.savePages(this, mPages, Settings.getInstance(this).configFile());
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        ConfigMngr.savePages(this, mPages, Settings.getInstance(this).configFile());
        super.onDestroy();
    }

    // auto enter pip mode
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        ConfigMngr.savePages(this, mPages, Settings.getInstance(this).configFile());
        _enterPipMode();
    }
    private void _enterPipMode() {
        Rational ratio = new Rational(9, 12);
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(ratio)
                .setSeamlessResizeEnabled(true)
                .build();
        enterPictureInPictureMode(params);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
        _showBtns(!isInPipMode);
    }

    // TODO move?
    public void onAddContactBtnClicked(View v) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 333);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        mContactPickerLauncher.launch(intent);
    }

    ////// ICaptureInput IMPL
    @Override
    public boolean setCaptureInput(final boolean captureReq) {
        // If current fragment does not support capture it can reject it
        final boolean capture = mFPager.setCaptureInput(captureReq);
        mBackPressedCb.setEnabled(capture);
        mUserActionsLayout.setCaptureInput(capture);

        //if capture was requested but not realized we don't write
        //release is currently never rejected
        //TODO it would be cleaner to have two methods here but later...
        if (!(captureReq && !capture)) {
            Toast.makeText(MainActivity.this, capture ? "CAPTURE" : "RELEASE", Toast.LENGTH_SHORT).show();
        }
        return capture;
    }

    /////// PAGE NAVIGATION
    public void nextPage() { mFPager.showNextFragment(); }
    public void previousPage() { mFPager.showPreviousFragment(); }
    ////// PAGE MANIPULATION
    private void _addPage() {
        if (mFNewPageIdx == NEW_PAGE_IDX_NONE) {
            _addNewPageFragment(mFPager.currentFragmentIdx() + 1, false);
        }
        else {
            Toast.makeText(this, "New page already exists at index: " + mFNewPageIdx, Toast.LENGTH_SHORT).show();
        }
    }
    private void _removePage() {
        final int currentFragmentId = mFPager.currentFragmentIdx();
        Log.d(CLASS_TAG, "Remove: " + mPages.toString() + "  " + currentFragmentId);

        mPages.remove(currentFragmentId);
        if (currentFragmentId == mFNewPageIdx) { _clearNewPage(); }
        if (mPages.isEmpty()) { _addNewPageFragment(0, true);} // replace current fragment with new one
        else { mFPager.removeFragment(currentFragmentId); }
    }
    public void reloadPagesFromConfig() {
        // clear all pages
        mFPager.removeAllFragments();
        mPages.clear();
        _clearNewPage();
        // load freash from config
        _loadPages();
    }

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

            if (id == R.id.action_add_page) { _addPage();}
            else if (id == R.id.action_remove_page) { _removePage(); }
            else if (id == R.id.action_enter_pip) { _enterPipMode(); }
            else if (id == R.id.config_show_btns) {
                boolean show = !item.isChecked();
                Settings.getInstance(this).setShowBtns(show);
                item.setChecked(show);
                _showBtns(show);
            }
            else if (id == R.id.config_list_configs) {
                ConfigurationListDialog.show(this);
            }
            return true;
        });

        mMenuVisible = true;
        menu.show();
    }

    //////
    private void _showBtns(final boolean showRequest) {
        final boolean show = showRequest && Settings.getInstance(this).showBtns();

        findViewById(R.id.menu_btn).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.call_btn).setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void _loadPages() {
//        mPages = Page.loadPages(this);
        mPages = ConfigMngr.loadPages(this, Settings.getInstance(this).configFile());

        // TODO: detect if config file was manhandled and has too many pages
        for (Page page : mPages) {
            final int idx = mFPager.numFragments();
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB, Page.PAGE_TYPE_CONTACTS:
                    FragmentBase fBase = FragmentBase.createFragment(idx, page.page_type);
                    fBase.updateData(page);
                    mFPager.addFragment(idx, fBase);
                    break;
                default:
                    continue;
            }
        }
        if (mFPager.numFragments() == 0) {
            // add new page fragment
            _addNewPageFragment(0, false);
        }
    }

    private boolean _addNewPageFragment(int idx, boolean replace) {
        if (mFNewPageIdx != NEW_PAGE_IDX_NONE) {
            return false;
        }
        FragmentNewPage fNewPage = (FragmentNewPage) FragmentBase.createFragment(mFNewPageIdx, FragmentBase.FRAGMENT_NEW_PAGE);
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
            return false;
        }
        mFNewPage = fNewPage;
        mFNewPageIdx = idx;
        mPages.add(idx, null); //blank page. not exported but we need it for index
        mFNewPage.setPageConfigredCb(new PageConfiguredCb());

        return true;
    }

    private ActivityResultLauncher<Intent> _createContactPickerLauncher() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    Page.Contact contact = null;
                    if (result.getResultCode() == FragmentActivity.RESULT_OK)
                        //This action is rare so we don't care about a potential performance hit when catching the exception
                        try (final Cursor cursor = this.getContentResolver().query(result.getData().getData(),
                                null, null, null, null)) {
                            cursor.moveToFirst();

                            contact = new Page.Contact(
                                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            );
                        } catch (IllegalArgumentException | NullPointerException e) {
                            Log.e(CLASS_TAG, "Failed to add contact " + e.getMessage());
                        }
                    if (contact == null) {
                        return;
                    }
                    int currentItem = mFPager.currentFragmentIdx();
                    Page page = mPages.get(currentItem); // if this is null there is a bug
                    if (page.contacts == null) {
                        page.contacts = new ArrayList<>();
                    }
                    page.contacts.add(contact);
                    mFPager.notifyFragmentDataChanged(currentItem, page);
                });
    }

    private void _clearNewPage() {
        if (mFNewPage != null) {
            mFNewPage.setPageConfigredCb(null);
        }
        mFNewPage = null;
        mFNewPageIdx = NEW_PAGE_IDX_NONE;
    }

    private class PageConfiguredCb implements FragmentNewPage.PageConfiguredCb {
        @Override
        public void onPageConfigured(Page page) {
            FragmentBase fBase = null;
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB -> {
                    fBase = FragmentBase.createFragment(mFNewPageIdx, FragmentBase.FRAGMENT_WEB_PAGE);
                    // todo set refreshRate
                    ((FragmentWebPage) fBase).setUrl(page.url);
                }
                case Page.PAGE_TYPE_CONTACTS -> {
//                    _showUnsupportedActionToast();
                    fBase = FragmentBase.createFragment(mFNewPageIdx, FragmentBase.FRAGMENT_CALLS);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 333);
                }
                default -> {} //keep the fragment
            }
            if (fBase != null) {
                MainActivity.this.mPages.set(mFNewPageIdx, page); //todo!! arrange correctly
                mFPager.replaceFragment(mFNewPageIdx, fBase);
                _clearNewPage();
            }
        }
    }

    private void _showUnsupportedActionToast() {
        Toast.makeText(this, "Unsupported action", Toast.LENGTH_LONG).show();
    }

    private void _loadDummyPages() {
        // temp
        int i = 0;
        for (; i < CarouselFragmentPager.MAX_VIEWS - 1; i++) {
            mFPager.addFragment(i,
                    FragmentBase.createFragment(i, FragmentBase.FRAGMENT_NEW_PAGE));
        }
        FragmentWebPage wp = (FragmentWebPage) FragmentBase.createFragment(i, FragmentBase.FRAGMENT_WEB_PAGE);
        wp.setUrl("https://archlinux.org");
        mFPager.addFragment(i++, wp);
    }
}
