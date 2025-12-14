package io.github.wandomium.viewcarousel.pager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    }

    @Override
    public void updateData(Page page) {
        mPage = page;
        if (getView() != null) {
            _setupBtns();
        }
    }

    public boolean onDirectCallBtnClicked(View v) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Missing permission", Toast.LENGTH_LONG).show();
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

    private void _setupBtns() {
        int i = 0;
        final int numContacts = mPage.contacts != null ? mPage.contacts.size() : 0;
        for (; i < numContacts; i++) {
            Page.Contact contact = mPage.contacts.get(i);
            mBtns[i].setVisibility(View.VISIBLE);
            mBtns[i].setText(contact.name());
            mBtns[i].setTag(contact); //TODO maybe use id??
            mBtns[i].setOnLongClickListener(this::onDirectCallBtnClicked);
        }
        for (; i < mBtns.length; i++) {
            mBtns[i].setVisibility(View.GONE);
            mBtns[i].setText("");
            mBtns[i].setTag("");
            mBtns[i].setOnLongClickListener(null);
        }
    }
}
