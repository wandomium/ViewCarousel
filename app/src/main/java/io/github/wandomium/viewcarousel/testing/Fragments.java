package io.github.wandomium.viewcarousel.testing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.wandomium.viewcarousel.R;

public class Fragments {
    public static final String ARG_ID = "id";

    public static Fragment createFragment(int id) {
        Fragment f = new TestFragment();
        Bundle args = new Bundle();
        // The object is just an integer.
        args.putInt(Fragments.ARG_ID, id);
        f.setArguments(args);

        return f;
    }

    public static class TestFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.demo_fragment, container, false);
            ((TextView) view.findViewById(R.id.text1)).setText(Integer.toString(getArguments().getInt(ARG_ID)));
            return view;
        }
    }
}
