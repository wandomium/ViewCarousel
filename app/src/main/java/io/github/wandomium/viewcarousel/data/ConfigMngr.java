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
package io.github.wandomium.viewcarousel.data;

import android.content.Context;
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
import java.util.regex.Pattern;

public class ConfigMngr
{
    private static final String CLASS_TAG = ConfigMngr.class.getSimpleName();

    private static final String SUFFIX = ".json";

    public static ArrayList<String> getConfigs(Context ctx)
    {
        File directory = ctx.getExternalFilesDir(null);
        ArrayList<String> configList = new ArrayList<>();

        if (directory != null) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) { // Ensure we aren't listing sub-folders
                        //configList.add(file.getName().replaceAll("(?i)\\.json$", ""));
                        configList.add(file.getName().replaceAll(Pattern.quote(SUFFIX) + "$", ""));
                    }
                }
            }
        }
        return configList;
    }

    public static boolean deleteConfig(Context ctx, final String configFname)
    {
        File file = new File(ctx.getExternalFilesDir(null), configFname + SUFFIX);
        return file.delete();
    }

    public static ArrayList<Page> loadPages(final Context ctx, final String configFname)
    {
        File file = new File(ctx.getExternalFilesDir(null), configFname + SUFFIX);

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
        File file = new File(ctx.getExternalFilesDir(null), configName + SUFFIX);
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
