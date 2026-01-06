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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.data.Page;
import io.github.wandomium.viewcarousel.ui.CountdownSnackbar;

public class FragmentCalls extends FragmentBase
{
    private static final String CLASS_TAG = FragmentCalls.class.getSimpleName();

    private static final int[] cBtnLayouts = {
        R.id.btn_call_contact0, R.id.btn_call_contact1, R.id.btn_call_contact2
    };

    private Button[] mDirectCallBtns = new Button[cBtnLayouts.length];
    private Button mAddContactBtn;

    private ActivityResultLauncher<Intent> mContactPickerLauncher;

    // call delay display
    public static final int CALL_DELAY_S = 2;
    private CountdownSnackbar mCountdownSnackbar;
    private Handler mHandler;
    private Runnable mMakeCallRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPage == null) {
            mPage = Page.createContactsPage(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        return inflater.inflate(R.layout.fragment_calls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 333);

        for (int i = 0; i < cBtnLayouts.length; i++) {
            mDirectCallBtns[i] = view.findViewById(cBtnLayouts[i]);
            _setDirectCallBtnEnabled(i, i < mPage.contacts.size());
        }

        /// contact select
        view.findViewById(R.id.add_contact_button).setOnClickListener(this::onAddContactBtnClicked);
        mContactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ContactResultCb());

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroyView() {
        // nullify all view references and callbacks that hold reference to fragment
        for (Button btn : mDirectCallBtns) {
            btn.setOnTouchListener(null);
        }
        mDirectCallBtns = null;
        requireView().findViewById(R.id.add_contact_button).setOnClickListener(null);

        // stop pending actions if any
        if (mMakeCallRunnable != null) {
            mHandler.removeCallbacks(mMakeCallRunnable);
            mMakeCallRunnable = null;
        }
        if (mCountdownSnackbar != null) {
            mCountdownSnackbar.dismiss();
            mCountdownSnackbar = null;
        }

        mHandler = null;
        mContactPickerLauncher = null;

        super.onDestroyView();
    }

    public void onAddContactBtnClicked(View ignored) {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 333);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        mContactPickerLauncher.launch(intent);
    }

    public boolean onDirectCallBtnTouch(View v, MotionEvent event) {
        if (mHandler == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mMakeCallRunnable = () -> _makeDirectCall((Page.Contact) v.getTag()); // can fail but then it is a bug and we want it to
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

    private void _makeDirectCall(final Page.Contact contact)
    {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getContext(), "Missing permission", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
        } else {
            // make call
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + contact.phone())); //+38640655943"));
            intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            startActivity(intent);
        }
    }

    private void _setDirectCallBtnEnabled(final int idx, final boolean enabled)
    {
        Page.Contact contact = enabled ? mPage.contacts.get(idx) : null;

        mDirectCallBtns[idx].setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDirectCallBtns[idx].setText(enabled ? contact.name() : "");
        mDirectCallBtns[idx].setTag(enabled ? contact : "");
        mDirectCallBtns[idx].setOnTouchListener(enabled ? this::onDirectCallBtnTouch : null);
    }

    // non-static class !!!
    private class ContactResultCb implements ActivityResultCallback<ActivityResult> {
        @Override
        public void onActivityResult(ActivityResult result) {
            Page.Contact contact = null;
            if (result.getResultCode() == FragmentActivity.RESULT_OK)
                //This action is rare so we don't care about a potential performance hit when catching the exception
                try (final Cursor cursor = requireContext().getContentResolver().query(result.getData().getData(),
                        null, null, null, null)) {
                    cursor.moveToFirst();

                    contact = new Page.Contact(
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    );
                } catch (IllegalArgumentException | NullPointerException e) {
                    e.printStackTrace();
                    Log.e(CLASS_TAG, "Failed to add contact " + e.getMessage());
                }
            if (contact == null) {
                return;
            }
            if (mPage.contacts == null) {
                mPage.contacts = new ArrayList<>();
            }
            mPage.contacts.add(contact);
            _setDirectCallBtnEnabled(mPage.contacts.size() - 1, true);

            mPageUpdatedCb.onFragmentDataUpdated(Page.PAGE_TYPE_CONTACTS, mPage);
        }
    }
}
