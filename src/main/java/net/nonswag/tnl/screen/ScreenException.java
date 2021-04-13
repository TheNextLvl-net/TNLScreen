package net.nonswag.tnl.screen;

import javax.annotation.Nonnull;

public class ScreenException extends Exception {

    public ScreenException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public ScreenException(@Nonnull String message) {
        super(message);
    }

    public ScreenException() {
    }
}
