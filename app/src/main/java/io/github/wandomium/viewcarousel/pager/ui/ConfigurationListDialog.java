package io.github.wandomium.viewcarousel.pager.ui;

import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.pager.MainActivity;
import io.github.wandomium.viewcarousel.pager.Settings;
import io.github.wandomium.viewcarousel.pager.data.ConfigMngr;

public class ConfigurationListDialog
{

    public static void show(MainActivity mainActivity)
    {
        final Settings SETTINGS = Settings.getInstance(mainActivity);

        final ArrayList<String> configs = ConfigMngr.getConfigs(mainActivity);
        final String currentConfig = SETTINGS.configFile();
        final int currentConfigIdx = configs.indexOf(currentConfig);

        new AlertDialog.Builder(mainActivity)
                .setTitle("Select configuration")
                .setSingleChoiceItems(configs.toArray(new String[0]), currentConfigIdx,
                        (dialog, which) -> SETTINGS.setConfigFile(configs.get(which)))
                .setPositiveButton("Create new", (dialog, which) -> {
                    dialog.dismiss();
                    showNewConfigDialog(mainActivity);
                }).setNegativeButton("OK", ((dialog, which) -> {
                    mainActivity.reloadPagesFromConfig();
                })).create().show();
    }

    public static void showNewConfigDialog(MainActivity mainActivity)
    {
        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Standard text input
        input.setHint(".json");
        new AlertDialog.Builder(mainActivity)
                .setTitle("Enter name")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    Settings.getInstance(mainActivity).setConfigFile(input.getText().toString());
                    mainActivity.reloadPagesFromConfig();
                }).create().show();
    }
}
