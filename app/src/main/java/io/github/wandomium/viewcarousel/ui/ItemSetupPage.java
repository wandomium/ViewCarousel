package io.github.wandomium.viewcarousel.ui;

import io.github.wandomium.viewcarousel.Page_old;

public class ItemSetupPage
{
    @FunctionalInterface
    public interface UrlSelectedCb {
        void onUrlSelected(final Page_old pageOld);
    }

}
