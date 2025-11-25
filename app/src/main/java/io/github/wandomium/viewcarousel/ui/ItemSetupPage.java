package io.github.wandomium.viewcarousel.ui;

import io.github.wandomium.viewcarousel.Page;

public class ItemSetupPage
{
    @FunctionalInterface
    public interface UrlSelectedCb {
        void onUrlSelected(final Page page);
    }

}
