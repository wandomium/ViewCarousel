package io.github.wandomium.viewcarousel.ui;

@FunctionalInterface
public interface ICaptureInput
{
    /**
     * @param captureReq requested capture state
     * @return actual capture state of the component. Can be rejected if unsupported
     */
    boolean setCaptureInput(final boolean captureReq);
}
