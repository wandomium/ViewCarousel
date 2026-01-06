package io.github.wandomium.viewcarousel.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.github.wandomium.viewcarousel.MainActivity;

public class DialogBugReport
{
    public static void show(MainActivity activity) {
        // Create a SpannableString for the message
        String message = "Please report issues here and include Version";
        SpannableString spannableString = new SpannableString(message);

        // Set the clickable part of the message (e.g., "Click here")
        int start = message.indexOf("here");
        int end = start + "here".length();

        String title;
        try {
            title = "Version " + activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            title = "Report Bug";
        }

        // Create the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(spannableString)
                .setPositiveButton("OK", null) // Optional positive button
                .setCancelable(true)
                .create();

        // Make the text clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wandomium/ViewCarousel/issues"));
                activity.startActivity(intent);

                dialog.dismiss();
            }
        };

        spannableString.setSpan(clickableSpan, start, end, 0);

        // Set the movement method to LinkMovementMethod to make the text clickable
        dialog.setOnShowListener(dialogInterface -> {
            TextView textView = dialog.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });

        dialog.show();
    }

}
