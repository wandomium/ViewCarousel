package io.github.wandomium.viewcarousel.pager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;
import io.github.wandomium.viewcarousel.pager.ui.ICaptureInput;
import io.github.wandomium.viewcarousel.testing.Fragments;

public abstract class FragmentBase extends Fragment implements ICaptureInput
{
    private static final String CLASS_TAG = FragmentBase.class.getSimpleName();

    protected static final String ARG_ID = "id";
    protected int mId;

    // TODO: unify this with Page IDs
    public static final int FRAGMENT_NEW_PAGE = 0;
    public static final int FRAGMENT_WEB_PAGE = 1;
    public static final int FRAGMENT_CALLS = 2;
    public static FragmentBase createFragment(int id, int type) {
        FragmentBase f = switch (type) {
            case FRAGMENT_WEB_PAGE -> new FragmentWebPage();
            case FRAGMENT_NEW_PAGE -> new FragmentNewPage();
            case FRAGMENT_CALLS -> new FragmentCalls();
            default -> throw new IllegalArgumentException("Invalid fragment type");
        };
        Bundle args = new Bundle();
        args.putInt(Fragments.ARG_ID, id);
        f.setArguments(args);
        return f;
    }

    public void updateData(Page page) {}
    public void onHide()  { Log.d(CLASS_TAG, "onHide"); }
    public void onShow()  { Log.d(CLASS_TAG, "onShow"); }

    @Override
    public boolean setCaptureInput(final boolean captureReq) {
        // new capture state can be rejected by the fragment if it is unsupported
        Log.d(CLASS_TAG, "captureInput: " + captureReq);
        return false; //unsupported by default
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mId = getArguments().getInt(ARG_ID);
        return inflater.inflate(R.layout.demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((TextView) view.findViewById(R.id.text1)).setText(Integer.toString(mId));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) { onHide(); }
        else { onShow(); }
    }
}
