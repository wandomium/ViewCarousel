package io.github.wandomium.viewcarousel.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class Page
{
    private static final String CLASS_TAG = Page.class.getSimpleName();

    private static final String CONFIG_FOLDER_NAME = "ViewCarousel";

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

//
//    public static void savePagesExt(Context context, final ArrayList<Page> pages, final String fname)
//    {
//        // create a new list so we don't mess up the original one
//        ArrayList<Page> pagesToSave;
//        if (pages == null) {
//            pagesToSave = new ArrayList<>();
//        }
//        else {
//            pagesToSave = new ArrayList<>(pages);
//            pagesToSave.removeIf(Objects::isNull);
//        }
//        // serialize data
//        Gson gson = new Gson();
//        final String data = gson.toJson(pagesToSave);
//
//        // write file
//        final ContentResolver resolver = context.getContentResolver();
//        final Uri fileUri = _getFileUri(fname, resolver);
//
//        if (fileUri != null) {
//            try (OutputStream outputStream = resolver.openOutputStream(fileUri, "wt")) {
//                outputStream.write(data.getBytes());
//                Log.d(CLASS_TAG,"Saving to " + fileUri.getPath());
//                Log.d(CLASS_TAG, data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // this needs to be simplified
//    private static Uri _getFileUri(final String fname, ContentResolver resolver)
//    {
//        String folderPath = Environment.DIRECTORY_DOCUMENTS + "/" + CONFIG_FOLDER_NAME;
//        Uri externalUri = MediaStore.Files.getContentUri("external");
//
//        // does the file exist?
//        String selection = MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ? AND " +
//                MediaStore.MediaColumns.DISPLAY_NAME + "=?";
//        String[] selectionArgs = new String[]{folderPath + "%", fname};
//
//        try (Cursor cursor = resolver.query(externalUri, null, selection, selectionArgs, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                // File exists! Get its URI
//                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
//                Log.d(CLASS_TAG, "file found: " + ContentUris.withAppendedId(externalUri, id).getPath());
//                return ContentUris.withAppendedId(externalUri, id);
//            }
//        }
//
//        // file does not exist, create
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fname);
//        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
//        values.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
//
//        return resolver.insert(externalUri, values);
//    }
}
