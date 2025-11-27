package io.github.wandomium.viewcarousel.demo;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.wandomium.viewcarousel.overrides.FragmentStateAdapterOverride;

public class DemoFragmentStateAdapter extends FragmentStateAdapter
{
    private static final String KEY_PREFIX_FRAGMENT = "f#";
    private static @NonNull String createKey(@NonNull String prefix, long id) {
        return prefix + id;
    }


    private static final String CLASS_TAG = DemoFragmentStateAdapter.class.getSimpleName();

    public static final int MAX_FRAGMENTS = 4;
    private final List<Fragment> fragmentList;
    private final Map<Integer, Fragment> fragmentMap;
    private final FragmentActivity mActivity;

    public DemoFragmentStateAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        mActivity = fragmentActivity;
        fragmentList = new ArrayList<>();
        fragmentMap = new HashMap<Integer, Fragment>();
//        fragmentList.add((new DemoFragment()));
//        fragmentList.add(createFragment(1));
//        fragmentList.add(createFragment(2));
//        fragmentList.add(createFragment(3));
//        fragmentList.add(createFragment(4));
//
//        Log.d(CLASS_TAG, "FragmentList consturctor: " + fragmentList.toString());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int).
        Log.d(CLASS_TAG, "createFragment: " + position);
        int realPosition = _getRealPosition(position);

//        final String fTag = "test_f_tag_" + realPosition;
        final String fTag = createKey(KEY_PREFIX_FRAGMENT, realPosition);

//        Fragment f = fragmentMap.get(realPosition);
        Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(fTag);
//        f = mActivity.getSupportFragmentManager().findFragmentById(realPosition);
        if (f != null) {
            Log.d(CLASS_TAG, "Existing fragment: " + position + " -> " + realPosition);
            return f;
        }

        f = new DemoFragment();
        Bundle args = new Bundle();
        // The object is just an integer.
        args.putInt(DemoFragment.ARG_OBJECT, position);
        f.setArguments(args);
//        fragmentList.add(fragment);
        fragmentMap.put(realPosition, f);
        Log.d(CLASS_TAG, "New fragment: " + position + " -> " + realPosition + " total: " + fragmentMap.size());

        return f;
    }


    private int _getRealPosition(int position) {
        return switch (position) {
            case 0 -> MAX_FRAGMENTS - 1;
            case (MAX_FRAGMENTS + 1) -> 0;
            default -> position - 1;
        };

//        if (position == 0) { return fragmentList.size() - 1; }
//        else if (position == fragmentList.size() - 1) { return 0; }
//        else { return position - 1; }
    }

    @Override
    public int getItemCount() {
        return MAX_FRAGMENTS + 2;
//        return fragmentList.isEmpty() ? 0 : fragmentList.size() + 2;
    }
//    @Override
//    public long getItemId(int position) {
//        return (long) _getRealPosition(position);
//    }
//
//    @Override
//    public boolean containsItem(long itemId) {
//        return itemId >= 0 && itemId < MAX_FRAGMENTS;
//    }


//    private int _getRealPosition(int position) {
//        return dataList.isEmpty() ? 0 : position % dataList.size();
//    }
//    @Override
//    public int getItemCount() {
//        return dataList.isEmpty() ? 0 : SCROOL_LOOP_SIZE;
//    }
//    @Override
//    public long getItemId(int position) {
//        return (long) _getRealPosition(position);
//    }
//    @Override
//    public boolean containsItem(long itemId) {
//        return itemId >= 0 && itemId < dataList.size();
//    }
}
