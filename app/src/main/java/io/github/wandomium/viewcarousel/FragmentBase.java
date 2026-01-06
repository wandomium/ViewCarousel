package io.github.wandomium.viewcarousel;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.internal.ApiFeature;

import com.google.gson.Gson;

import io.github.wandomium.viewcarousel.data.Page;
import io.github.wandomium.viewcarousel.ui.ICaptureInput;

public abstract class FragmentBase extends Fragment implements ICaptureInput
{
    private static final String CLASS_TAG = FragmentBase.class.getSimpleName();

    protected static final String ARG_ID = "id";
    protected static final String PAGE = "page";
    protected int mId;

    protected Page mPage;
    protected FragmentDataUpdatedCb mPageUpdatedCb;

    @FunctionalInterface
    public interface FragmentDataUpdatedCb {
        void onFragmentDataUpdated(int id, int type, Page page); // type is here for hacks because we need a no-arg constructor
    }

    public static FragmentBase createFragment(int id, Page page) {
        FragmentBase f = switch (page != null ? page.page_type : Page.PAGE_TYPE_UNKNOWN) {
            case Page.PAGE_TYPE_WEB -> new FragmentWebPage();
            case Page.PAGE_TYPE_CONTACTS -> new FragmentCalls();
            case Page.PAGE_TYPE_UNKNOWN -> new FragmentNewPage();
            default -> throw new IllegalArgumentException("Invalid page type");
        };
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(PAGE, new Gson().toJson(page));
        f.setArguments(args);
        return f;
    }

    public Page getData() { return mPage; }
    public void onHide()  { Log.d(CLASS_TAG, "onHide"); }
    public void onShow()  { Log.d(CLASS_TAG, "onShow"); }

    @Override
    public boolean setCaptureInput(final boolean captureReq) {
        // new capture state can be rejected by the fragment if it is unsupported
        Log.d(CLASS_TAG, "captureInput: " + captureReq);
        return false; //unsupported by default
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getInt(ARG_ID);
        mPage = new Gson().fromJson(getArguments().getString(PAGE), Page.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentDataUpdatedCb) {
            mPageUpdatedCb = (FragmentDataUpdatedCb) context;
        }
        else {
            throw new IllegalArgumentException("Context must implement FragmentDataUpdatedCb");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((TextView) view.findViewById(R.id.text1)).setText(Integer.toString(mId));
    }

    @Override
    public void onDestroy() {
        mPageUpdatedCb = null;
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) { onHide(); }
        else { onShow(); }
    }
}
