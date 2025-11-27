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
import io.github.wandomium.viewcarousel.testing.Fragments;

public class BaseFragment extends Fragment
{
    private static final String CLASS_TAG = BaseFragment.class.getSimpleName();

    private static final String ARG_ID = "id";
    private int mId;
    public static Fragment createFragment(int id) {
        Fragment f = new Fragments.TestFragment();
        Bundle args = new Bundle();
        args.putInt(Fragments.ARG_ID, id);
        f.setArguments(args);
        return f;
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
    public void onResume() {
        Log.d(CLASS_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(CLASS_TAG, "onPause");
        super.onPause();
    }
}
