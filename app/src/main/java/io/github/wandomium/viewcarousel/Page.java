package io.github.wandomium.viewcarousel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class Page
{
    private static final String CLASS_TAG = Page.class.getSimpleName();

    public int refreshRateSec = 15;
    public final String url;

    public Page(@NonNull final String url) {
        this.url = Objects.requireNonNull(url);
    }

    private static String _getConfigFilename() {
        return "config.json";
    }

    public static ArrayList<Page> loadPages(final Context ctx) {
        File file = new File(ctx.getFilesDir(), _getConfigFilename());
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

    public static void savePages(final Context ctx, ArrayList<Page> pages) {
        File file = new File(ctx.getFilesDir(), _getConfigFilename());
        Gson gson = new Gson();

        pages.removeIf(Objects::isNull);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(pages, writer); // Serialize ArrayList to JSON file
            Log.d(CLASS_TAG,"Saving to " + file.getAbsolutePath());
            Log.d(CLASS_TAG, pages == null ? "null" : gson.toJson(pages));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
