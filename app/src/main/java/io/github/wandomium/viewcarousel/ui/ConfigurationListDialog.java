package io.github.wandomium.viewcarousel.ui;

import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import io.github.wandomium.viewcarousel.MainActivity;
import io.github.wandomium.viewcarousel.Settings;
import io.github.wandomium.viewcarousel.data.ConfigMngr;

public class ConfigurationListDialog
{

    public static void show(MainActivity mainActivity)
    {
        final Settings SETTINGS = Settings.getInstance(mainActivity);

        ArrayList<String> configs = ConfigMngr.getConfigs(mainActivity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_single_choice,
                configs
        );

        String currentConfig = SETTINGS.configFile();
        int currentConfigIdx = configs.indexOf(currentConfig);

        AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
                .setTitle("Select configuration")
                .setSingleChoiceItems(adapter, currentConfigIdx, null)
                .setNeutralButton("Create new", (dialog, which) -> {
                    dialog.dismiss();
                    showNewConfigDialog(mainActivity);})
                .setNegativeButton("Delete", null)
                .setPositiveButton("Select", ((dialog, which) -> {
                    final int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                    SETTINGS.setConfigFile(configs.get(selected));
                    mainActivity.reloadPagesFromConfig();
                })).create();

        // hackish way to prevent dialog from disappearing when config is deleted
        // user must select a new one
        // show needs to be called so that buttons are created first
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v) -> {
            final int selected = alertDialog.getListView().getCheckedItemPosition();
            if (ConfigMngr.deleteConfig(mainActivity, configs.get(selected))) {
                configs.remove(configs.get(selected));
                alertDialog.getListView().setItemChecked(0, true);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static void showNewConfigDialog(MainActivity mainActivity)
    {
        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Standard text input
//        input.setHint(".json");
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
