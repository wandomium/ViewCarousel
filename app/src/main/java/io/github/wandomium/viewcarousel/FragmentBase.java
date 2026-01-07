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

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.util.Objects;

import io.github.wandomium.viewcarousel.data.Page;
import io.github.wandomium.viewcarousel.ui.ICaptureInput;

public abstract class FragmentBase extends Fragment implements ICaptureInput
{
    private static final String CLASS_TAG = FragmentBase.class.getSimpleName();

    protected static final String ARG_ID = "id";
    protected static final String ARG_PAGE = "page";

    protected int mId = -1;
    protected Page mPage;
    protected FragmentDataUpdatedCb mPageUpdatedCb;

    public enum Type {
        WEB, DIALER, NEW_PAGE;

        static Type fromInt(int which) {
            return switch (which) {
                case Page.PAGE_TYPE_WEB -> WEB;
                case Page.PAGE_TYPE_CONTACTS -> DIALER;
                default -> NEW_PAGE;
            };
        }
    }

    @FunctionalInterface
    public interface FragmentDataUpdatedCb {
        void onFragmentDataUpdated(Type type, Page page);
    }
    public void setPageUpdatedCb(FragmentDataUpdatedCb cb) {
        mPageUpdatedCb = cb;
    }


    public static FragmentBase createFragment(final int id, final Page page) {
        FragmentBase f = switch (page != null ? Type.fromInt(page.page_type) : Type.NEW_PAGE) {
            case WEB -> new FragmentWebPage();
            case DIALER -> new FragmentCalls();
            case NEW_PAGE -> new FragmentNewPage();
        };
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_PAGE, new Gson().toJson(page));
        f.setArguments(args);
        return f;
    }

    public Page getData() { return mPage; }
    public void onHide()  { Log.d(CLASS_TAG, "onHide"); }
    public void onShow()  { Log.d(CLASS_TAG, "onShow"); }

    @Override
    public boolean setCaptureInput(final boolean captureReq) { return false;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mId = requireArguments().getInt(ARG_ID);
        mPage = new Gson().fromJson(
            Objects.requireNonNullElseGet(
                savedInstanceState, this::requireArguments).getString(ARG_PAGE), Page.class);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_PAGE, new Gson().toJson(mPage));
    }

    @Override
    public void onDetach() {
        mPageUpdatedCb = null;
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) { onHide(); }
        else { onShow(); }
    }
}
