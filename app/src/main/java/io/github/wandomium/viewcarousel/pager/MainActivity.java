package io.github.wandomium.viewcarousel.pager;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Rational;
import android.view.MenuItem;
import android.view.View;
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

import io.github.wandomium.viewcarousel.Page_old;
import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;
import io.github.wandomium.viewcarousel.pager.ui.CarouselFragmentPager;

public class MainActivity extends AppCompatActivity
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private OnBackPressedCallback mBackPressedCb;
    private CarouselFragmentPager mFPager;
    private ArrayList<Page> mPages;

    private final int NEW_PAGE_IDX_NONE = -1;
    private FragmentNewPage mFNewPage = null;
    private int mFNewPageIdx = NEW_PAGE_IDX_NONE;

    private ActivityResultLauncher<Intent> mContactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity_main);

        mFPager = findViewById(R.id.carousel_pager);
        mFPager.setFragmentManager(getSupportFragmentManager());

        // capture and release focus
        mFPager.setCaptureInputListener(() -> {
            mBackPressedCb.setEnabled(true);
            findViewById(R.id.menu_btn).setVisibility(View.GONE);
            findViewById(R.id.call_btn).setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
        });
        mBackPressedCb = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                mFPager.captureInput(false);
                setEnabled(false); // system handled back gesture
                findViewById(R.id.menu_btn).setVisibility(View.VISIBLE);
                findViewById(R.id.call_btn).setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(mBackPressedCb);

        mContactPickerLauncher = _createContactPickerLauncher();

        _loadPages();
    }

    @Override
    protected void onPause() {
        Page.savePages(this, mPages);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Page.savePages(this, mPages);
        super.onDestroy();
    }

    // auto enter pip mode
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        _enterPipMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPipMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPipMode, newConfig);
        findViewById(R.id.menu_btn).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
        findViewById(R.id.call_btn).setVisibility(isInPipMode ? View.GONE : View.VISIBLE);
    }

    public void addContactBtnClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        mContactPickerLauncher.launch(intent);
    }

    public void onMenuBtnClicked(View view) {
//        findViewById(R.id.menu_btn).setOnClickListener((v)->openOptionsMenu());
        PopupMenu menu = new PopupMenu(MainActivity.this, view);
        menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this::_handleMenuSelection);
        menu.show();
    }

    public void onCallBtnClicked(View v) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:"));
        startActivity(dialIntent);
//        if (dialIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(dialIntent);
//        }
    }

    private void _loadPages() {
        mPages = Page.loadPages(this);

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

    private void _enterPipMode() {
        Rational ratio = new Rational(9, 12);
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(ratio)
                .setSeamlessResizeEnabled(true)
                .build();
        enterPictureInPictureMode(params);
    }

    /** @noinspection SameReturnValue*/
    private boolean _handleMenuSelection(MenuItem item) {
        final int id = item.getItemId();
        final int currentFragmentId = mFPager.currentFragment();

        if (id == R.id.action_add_page) {
            // only add new page if there isn't an existing one
            if (mFNewPageIdx == NEW_PAGE_IDX_NONE) {
                _addNewPageFragment(currentFragmentId + 1, false);
            }
            else {
                Toast.makeText(this, "New page already exsists at index: " + mFNewPageIdx, Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.action_remove_page) {
            Log.d(CLASS_TAG, mPages.toString() + "  " + currentFragmentId);
            mPages.remove(currentFragmentId);
            if (mPages.isEmpty()) {
                _addNewPageFragment(0, true);
            }
            else {
                mFPager.removeFragment(currentFragmentId);
            }
        }
        else if (id == R.id.action_enter_pip) {
            _enterPipMode();
        }

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
                    int currentItem = mFPager.currentFragment();
                    Page page = mPages.get(currentItem); // if this is null there is a bug
                    if (page.contacts == null) {
                        page.contacts = new ArrayList<>();
                    }
                    page.contacts.add(contact);
                    mFPager.notifyFragmentDataChanged(currentItem, page);
                });
    }

    private class PageConfiguredCb implements FragmentNewPage.PageConfiguredCb {
        @Override
        public void onPageConfigured(Page page) {
            // TODO support contacts
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
                // replace new page with selected page
                mFPager.replaceFragment(mFNewPageIdx, fBase);
                // clear new page
                mFNewPage.setPageConfigredCb(null);
                mFNewPage = null;
                mFNewPageIdx = NEW_PAGE_IDX_NONE;
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
