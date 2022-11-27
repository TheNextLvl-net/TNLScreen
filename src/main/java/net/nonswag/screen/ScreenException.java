package net.nonswag.screen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ScreenException extends Exception {

    public ScreenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScreenException(String message) {
        super(message);
    }

    public ScreenException() {
    }
}
