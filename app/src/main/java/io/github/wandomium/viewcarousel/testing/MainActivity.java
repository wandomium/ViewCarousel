package io.github.wandomium.viewcarousel.testing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import io.github.wandomium.viewcarousel.R;

public class MainActivity extends AppCompatActivity {

    private View mainView; // Root view to detect swipes
    private TextView pageNumberTextView;

    private List<Fragment> fragments;
    private int currentFragment = 0;

    private final int PAGE_ID_DISPLAY_MS = 2000;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mPageIdDisplay = new Runnable() {
        @Override
        public void run() {
            pageNumberTextView.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing_activity_main);
        mainView = findViewById(R.id.root_view);
        pageNumberTextView = findViewById(R.id.page_indicator);

        fragments = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            fragments.add(Fragments.createFragment(i));
        }

        // Initially load FragmentOne
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragments.get(currentFragment))
                .commit();

        mainView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() { //== swipeNext
                if (currentFragment == fragments.size() - 1) {
                    currentFragment = 0;
                }
                else {
                    currentFragment += 1;
                }
                switchFragment(fragments.get(currentFragment), true);
            }

            @Override
            public void onSwipeRight() {
                if (currentFragment == 0) {
                    currentFragment = fragments.size() - 1;
                }
                else {
                    currentFragment -= 1;
                }
                switchFragment(fragments.get(currentFragment), false);

            }
        });
    }

    private void switchFragment(Fragment fragment, boolean rightIn) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (rightIn) {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right, //enter animation for new fragment
                    R.anim.slide_out_left  //exit animation for old fragment
//                    R.anim.slide_in_left,
//                    R.anim.slide_out_right
            );
        } else {
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
//                    R.anim.slide_in_right,
//                    R.anim.slide_out_left
            );
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // this would allow user to use back button

//        fragment.setExitTransition(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                super.onAnimationEnd(animator);
//                pageNumberTextView.setVisibility(View.GONE);
//            }
//        });

        mHandler.removeCallbacks(mPageIdDisplay);
        pageNumberTextView.setText((currentFragment+1) + "/" + fragments.size());
        pageNumberTextView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mPageIdDisplay, PAGE_ID_DISPLAY_MS);

        transaction.commit();
    }

}

