package io.github.wandomium.viewcarousel.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        ((TextView) view.findViewById(R.id.text1))
                .setText(Integer.toString(args.getInt(ARG_OBJECT)));
        Log.d(CLASS_TAG, "Fragment created: " + args.getInt(ARG_OBJECT));
    }
}
