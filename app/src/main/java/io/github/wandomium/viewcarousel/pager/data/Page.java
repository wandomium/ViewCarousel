package io.github.wandomium.viewcarousel.pager.data;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Page
{
    private static final String CLASS_TAG = Page.class.getSimpleName();
    private static final String CONFIG_FNAME_DEFAULT = "config.json";

    public final static int DEFAULT_REFRESH_RATE_MIN = 15;

    public final static int PAGE_TYPE_WEB = 1;
    public final static int PAGE_TYPE_CONTACTS = 2;

    //////
    // Handy gson, not exporting static and null fields
    public final int page_type;
    // web page
    public final String url;
    public Integer refresh_rate;
    // contacts
    public record Contact(String name, String phone) {};
    public ArrayList<Contact> contacts;
    //////

    private Page(int pageType, String url, Integer refreshRate, ArrayList<Contact> contacts) {
        this.page_type = pageType;
        this.url = url;
        this.refresh_rate = refreshRate;
        this.contacts = contacts;
    }

    public static Page createWebPage(@NonNull String url, @Nullable Integer refreshRate) {
        return new Page(PAGE_TYPE_WEB,
            Objects.requireNonNull(url),
                refreshRate == null ? DEFAULT_REFRESH_RATE_MIN : refreshRate,
                null);
    }

    public static Page createContactsPage(@Nullable ArrayList<Contact> contacts) {
        return new Page(PAGE_TYPE_CONTACTS, null, null, contacts);
    }

    public static ArrayList<Page> loadPages(final Context ctx) {
        Log.d(CLASS_TAG, "external files dir: " + ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        Log.d(CLASS_TAG, "files dir: " + ctx.getFilesDir());
        File file = new File(ctx.getExternalFilesDir(null), CONFIG_FNAME_DEFAULT);
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

    public static void savePages(final Context ctx, final ArrayList<Page> pages) {
        File file = new File(ctx.getExternalFilesDir(null), CONFIG_FNAME_DEFAULT);
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
            Log.d(CLASS_TAG, gson.toJson(pages));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
