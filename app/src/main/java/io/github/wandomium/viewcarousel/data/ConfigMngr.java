package io.github.wandomium.viewcarousel.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class ConfigMngr
{
    private static final String CLASS_TAG = ConfigMngr.class.getSimpleName();

    public static final String CONFIG_FNAME_DEFAULT = "config.json";

    public static ArrayList<String> getConfigs(Context ctx)
    {
        File directory = ctx.getExternalFilesDir(null);

        File[] files = directory.listFiles();
        ArrayList<String> configList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) { // Ensure we aren't listing sub-folders
                    configList.add(file.getName());
                }
            }
        }
        return configList;
    }

    public static ArrayList<Page> loadPages(final Context ctx, final String configFname) {
        Log.d(CLASS_TAG, "external files dir: " + ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        Log.d(CLASS_TAG, "files dir: " + ctx.getFilesDir());
        File file = new File(ctx.getExternalFilesDir(null), configFname);

        Gson gson = new Gson();
        ArrayList<Page> pages = null;

        try (FileReader reader = new FileReader(file)) {
            Type userListType = new TypeToken<ArrayList<Page>>() {}.getType();
            pages = gson.fromJson(reader, userListType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(CLASS_TAG, "Loading from: " + file.getAbsolutePath());
        Log.d(CLASS_TAG, pages == null ? "null" : gson.toJson(pages));

        return pages == null ? new ArrayList<>() : pages;
    }

    public static void savePages(final Context ctx, final ArrayList<Page> pages, final String configName) {
        File file = new File(ctx.getExternalFilesDir(null), configName);
        Gson gson = new Gson();

        // create a new list so we don't mess up the original one
        ArrayList<Page> pagesToSave;
        if (pages == null) {
            pagesToSave = new ArrayList<>();
        }
        else {
            pagesToSave = new ArrayList<>(pages);
            pagesToSave.removeIf(Objects::isNull);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(pagesToSave, writer); // Serialize ArrayList to JSON file
            Log.d(CLASS_TAG,"Saving to " + file.getAbsolutePath());
            Log.d(CLASS_TAG, gson.toJson(pagesToSave));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
