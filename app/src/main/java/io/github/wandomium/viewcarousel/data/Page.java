package io.github.wandomium.viewcarousel.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class Page {
    @SuppressWarnings("unused")
    private static final String CLASS_TAG = Page.class.getSimpleName();

    public final static int DEFAULT_REFRESH_RATE_MIN = 15;

    public final static int PAGE_TYPE_UNKNOWN = -1;
    public final static int PAGE_TYPE_WEB = 1;
    public final static int PAGE_TYPE_CONTACTS = 2;

    /// ///
    // Handy gson, not exporting static and null fields
    public final int page_type;
    // web page
    public final String url;
    public final int refresh_rate;

    // contacts
    public record Contact(String name, String phone) {
    }

    public ArrayList<Contact> contacts;

    /// ///

    private Page(int pageType, String url, Integer refreshRate, ArrayList<Contact> contacts) {
        this.page_type = pageType;
        this.url = url;
        this.refresh_rate = refreshRate != null ? refreshRate : DEFAULT_REFRESH_RATE_MIN;
        this.contacts = contacts;
    }

    public static Page createWebPage(@NonNull String url, @Nullable Integer refreshRate) {
        return new Page(PAGE_TYPE_WEB,
                Objects.requireNonNull(url),
                refreshRate == null ? DEFAULT_REFRESH_RATE_MIN : refreshRate,
                null);
    }

    public static Page createContactsPage(@Nullable ArrayList<Contact> contacts) {
        return new Page(PAGE_TYPE_CONTACTS, null, null, contacts != null ? contacts : new ArrayList<>());
    }
}
