package io.github.wandomium.viewcarousel.pager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import io.github.wandomium.viewcarousel.R;
import io.github.wandomium.viewcarousel.pager.data.Page;

public class FragmentCalls extends FragmentBase
{
    private static final String CLASS_TAG = FragmentCalls.class.getSimpleName();

    private static final int[] cBtnLayouts = {
        R.id.btn_call_contact0, R.id.btn_call_contact1, R.id.btn_call_contact2
    };


    private Button[] mBtns = new Button[cBtnLayouts.length];
    private Page mPage = null;

    public static final int CALL_DELAY_S = 2;
    private CountdownSnackbar mCountdownSnackbar;
    private Handler mHandler;
    private Runnable mMakeCallRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        return inflater.inflate(R.layout.item_calls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        for (int i = 0; i < cBtnLayouts.length; i++) {
            mBtns[i] = view.findViewById(cBtnLayouts[i]);
        }
        if (mPage != null) {
            _setupBtns();
        }

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroyView() {
        if (mMakeCallRunnable != null) {
            mHandler.removeCallbacks(mMakeCallRunnable);
        }
        if (mCountdownSnackbar != null) {
            mCountdownSnackbar.dismiss();
        }

        mHandler = null;
        mMakeCallRunnable = null;
        mCountdownSnackbar = null;

        super.onDestroyView();
    }

    @Override
    public void updateData(Page page) {
        mPage = page;
        if (getView() != null) {
            _setupBtns();
        }
    }

    public boolean onDirectCallBtnLongClick(View v) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getContext(), "Missing permission", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
        } else {
            // make call
            if (v.getTag() == null) {
                Log.e(CLASS_TAG, "No number, cannot call");
                return true; // we don't want to forward this
            }
            Page.Contact contact = (Page.Contact) v.getTag();

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + contact.phone())); //+38640655943"));
            intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            startActivity(intent);
        }

        return true;
    }

    public void onDirectCallBtnShortClick(View v) {
        Toast toast = Toast.makeText(getContext(), "Long press to call", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    public boolean onDirectCallBtnTouch(View v, MotionEvent event) {
        if (mHandler == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mMakeCallRunnable = () -> onDirectCallBtnLongClick(v);
                if (mCountdownSnackbar == null) {
                    mCountdownSnackbar = new CountdownSnackbar(getView()); // attach it to fragment view and keep it
                }
                mCountdownSnackbar.show();
                mHandler.postDelayed(mMakeCallRunnable, CALL_DELAY_S * 1000);
                return true;
            }
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mHandler.removeCallbacks(mMakeCallRunnable);
                mCountdownSnackbar.dismiss();
                mMakeCallRunnable = null;
                return true;
            }
        }
        return false;
    }

    private void _setupBtns() {
        int i = 0;
        final int numContacts = mPage.contacts != null ? mPage.contacts.size() : 0;
        for (; i < numContacts; i++) {
            Page.Contact contact = mPage.contacts.get(i);
            mBtns[i].setVisibility(View.VISIBLE);
            mBtns[i].setText(contact.name());
            mBtns[i].setTag(contact); //TODO maybe use id??
//            mBtns[i].setOnLongClickListener(this::onDirectCallBtnLongClick);
            mBtns[i].setOnTouchListener(this::onDirectCallBtnTouch);
        }
        for (; i < mBtns.length; i++) {
            mBtns[i].setVisibility(View.GONE);
            mBtns[i].setText("");
            mBtns[i].setTag("");
            mBtns[i].setOnLongClickListener(null);
        }
    }

    private static class CountdownSnackbar {
        private final Snackbar mSnackbar;
        private final CountDownTimer mTimer;

        public CountdownSnackbar(View v) {
            final String message = "Long press for speaker call    ";
            mSnackbar = Snackbar.make(v, message, Snackbar.LENGTH_SHORT);
            View snackbarView = mSnackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();

            // move it to top
            params.gravity = Gravity.TOP;
            params.setMargins(100, 150, 100, 0);
            snackbarView.setLayoutParams(params);

            // add progress bar
            ProgressBar progressBar = new CircularProgressIndicator(v.getContext());
            progressBar.setMax(CALL_DELAY_S * 1000);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) mSnackbar.getView();
            layout.addView(progressBar, 1);

            // add timer for progress bar
            mTimer = new CountDownTimer(CALL_DELAY_S * 1000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update the text every 1 second
                    int remaining = (int) (millisUntilFinished / 1000) + 1;
                    mSnackbar.setText("  " + remaining + "s       " + message);
                    progressBar.setProgress((int)millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    // Hide the snackbar when time is up
                    if (mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                }
            };
        }

        public void show() {
            mSnackbar.show();
            mTimer.start();
        }

        public void dismiss() {
            mSnackbar.dismiss();
            mTimer.cancel();
        }
    }
}
