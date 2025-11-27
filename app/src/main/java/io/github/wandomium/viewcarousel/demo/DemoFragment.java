package io.github.wandomium.viewcarousel.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.wandomium.viewcarousel.R;

// Instances of this class are fragments representing a single
// object in the collection.
public class DemoFragment extends Fragment {
    private static final String CLASS_TAG = DemoFragment.class.getSimpleName();

    public static final String ARG_OBJECT = "object";
    private int id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        id = getArguments().getInt(ARG_OBJECT);
        if (id != 0 && id <= DemoFragmentStateAdapter.MAX_FRAGMENTS) {
            return inflater.inflate(R.layout.demo_fragment, container, false);
        }
        else {
            FrameLayout rootLayout = new FrameLayout(requireContext());

            // Ensure the root layout takes up the entire space
            rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            // Optional: Set a background color for visual clarity
            rootLayout.setBackgroundColor(Color.LTGRAY);

            return rootLayout;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (id != 0 && id <= DemoFragmentStateAdapter.MAX_FRAGMENTS) {
            ((TextView) view.findViewById(R.id.text1))
                    .setText(Integer.toString(id));
        }
    }
//
//    @Override
//    public void setInitialSavedState(@Nullable SavedState state) { }
}
