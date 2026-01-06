/**
 * This file is part of ViewCarousel.
 * <p>
 * ViewCarousel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * ViewCarousel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ViewCarousel. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.wandomium.viewcarousel.ui;

import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import io.github.wandomium.viewcarousel.FragmentCalls;

public class CountdownSnackbar {
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
        progressBar.setMax(FragmentCalls.CALL_DELAY_S * 1000);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) mSnackbar.getView();
        layout.addView(progressBar, 1);

        // add timer for progress bar
        mTimer = new CountDownTimer(FragmentCalls.CALL_DELAY_S * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                final int sRemaining = (int) (millisUntilFinished / 1000) + 1;
                mSnackbar.setText("  " + sRemaining + "s       " + message);
                progressBar.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
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
