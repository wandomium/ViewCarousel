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
package io.github.wandomium.viewcarousel;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import io.github.wandomium.viewcarousel.data.Page;

public class FragmentNewPage extends FragmentBase
{
    private static final String URL_INIT_TEXT = "https://";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        return inflater.inflate(R.layout.fragment_new_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_add_web_page)
            .setOnClickListener((ignored) -> _showAddWebPageDialog());
        view.findViewById(R.id.btn_add_call_page)
            .setOnClickListener((ignored) -> {
                if (mPageUpdatedCb != null) {
                    mPageUpdatedCb.onFragmentDataUpdated(Type.NEW_PAGE, Page.createContactsPage(null));}
            });
    }

    private void _showAddWebPageDialog()
    {
        // 2. Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_add_web_page, null);

        // Configure URL text
        TextInputEditText urlInput = customView.findViewById(R.id.url);
        urlInput.setText(URL_INIT_TEXT);

        // Configure the refresh rate selector
        NumberPicker refreshRate = customView.findViewById(R.id.refresh_rate);
        refreshRate.setMinValue(0);
        refreshRate.setMaxValue(100);
        refreshRate.setValue(Page.DEFAULT_REFRESH_RATE_MIN);

        // create and show dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Enter URL and refresh rate in minutes")
                .setView(customView)
                .setPositiveButton("OK", (id, l) -> {
                    if (urlInput.getText() != null) {
                        String url = urlInput.getText().toString();
                        if (!url.isEmpty() && !url.equals(URL_INIT_TEXT)) {
                            if (mPageUpdatedCb != null) {
                                mPageUpdatedCb.onFragmentDataUpdated(
                                    Type.NEW_PAGE,
                                    Page.createWebPage(url, refreshRate.getValue()));}
                        }
                    }
                    id.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
