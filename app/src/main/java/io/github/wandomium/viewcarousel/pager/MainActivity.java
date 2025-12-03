package io.github.wandomium.viewcarousel.pager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;
import io.github.wandomium.viewcarousel.pager.ui.CarouselFragmentPager;

public class MainActivity extends AppCompatActivity
{
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private OnBackPressedCallback mBackPressedCb;
    private CarouselFragmentPager mFPager;
    private ArrayList<Page> mPages;

    private FragmentNewPage mFNewPage = null;
    private int mFNewPageIdx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity_main);

        mFPager = findViewById(R.id.carousel_pager);
        mFPager.setFragmentManager(getSupportFragmentManager());

        // capture and release focus
        mFPager.setCaptureInputListener(() -> {
            mBackPressedCb.setEnabled(true);
            Toast.makeText(MainActivity.this, "CAPTURE", Toast.LENGTH_SHORT).show();
        });
        mBackPressedCb = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                mFPager.captureInput(false);
                setEnabled(false); // system handled back gesture
                Toast.makeText(MainActivity.this, "RELEASE", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(mBackPressedCb);

        _loadPages();
    }

    private void _loadPages() {
        mPages = Page.loadPages(this);
        Log.d(CLASS_TAG, "mPages is null: " + (mPages == null));

        // TODO: detect if config file was manhandled and has too many pages
        for (Page page : mPages) {
            final int idx = mFPager.numFragments();
            if (page.page_type == Page.PAGE_TYPE_WEB) {
                FragmentWebPage fWp = (FragmentWebPage) FragmentBase
                        .createFragment(idx, FragmentBase.FRAGMENT_WEB_PAGE);
                fWp.setUrl(page.url);
                mFPager.addFragment(idx, fWp);
            }
        }
        if (mFPager.numFragments() == 0) {
            // add new page fragment
            _addNewPageFragment(0);
        }
    }

    private void _addNewPageFragment(int idx) {
        mFNewPageIdx = idx;
        mFNewPage = (FragmentNewPage) FragmentBase.createFragment(0, FragmentBase.FRAGMENT_NEW_PAGE);
        mFPager.addFragment(0, mFNewPage);
        mFNewPage.setPageConfigredCb((page) -> {
            Log.d(CLASS_TAG, MainActivity.this.mPages.toString() + " is null " + (mPages == null));
            // TODO support contacts
            FragmentBase fBase = null;
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB -> {
                    fBase = FragmentBase.createFragment(mFNewPageIdx, FragmentBase.FRAGMENT_WEB_PAGE);
                    // todo set refreshRate
                    ((FragmentWebPage) fBase).setUrl(page.url);
                }
                case Page.PAGE_TYPE_CONTACTS -> {
                    Toast.makeText(MainActivity.this, "UNSUPPORTED FRAGMENT TYPE", Toast.LENGTH_LONG).show();
                }
                default -> {} //keep the fragment
            }
            if (fBase != null) {
                MainActivity.this.mPages.add(page);
                // replace new page with selected page
                mFPager.removeFragment(mFNewPageIdx);
                mFPager.addFragment(mFNewPageIdx, fBase);
                mFNewPage = null;
                mFNewPageIdx = -1;
            }
        });
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

    @Override
    protected void onPause() {
        Page.savePages(this, mPages);
        super.onPause();
    }

    private class PageConfiguredCb implements FragmentNewPage.PageConfiguredCb {
        @Override
        public void onPageConfigured(Page page) {
            Log.d(CLASS_TAG, MainActivity.this.mPages.toString() + " is null " + (mPages == null));
            // TODO support contacts
            FragmentBase fBase = null;
            switch (page.page_type) {
                case Page.PAGE_TYPE_WEB -> {
                    fBase = FragmentBase.createFragment(mFNewPageIdx, FragmentBase.FRAGMENT_WEB_PAGE);
                    // todo set refreshRate
                    ((FragmentWebPage) fBase).setUrl(page.url);
                }
                case Page.PAGE_TYPE_CONTACTS -> {
                    Toast.makeText(MainActivity.this, "UNSUPPORTED FRAGMENT TYPE", Toast.LENGTH_LONG).show();
                }
                default -> {} //keep the fragment
            }
            if (fBase != null) {
                MainActivity.this.mPages.add(page);
                // replace new page with selected page
                mFPager.removeFragment(mFNewPageIdx);
                mFPager.addFragment(mFNewPageIdx, fBase);
                mFNewPage = null;
                mFNewPageIdx = -1;
            }
        }
    }
}
