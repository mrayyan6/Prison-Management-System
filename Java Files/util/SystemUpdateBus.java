package com.prison.util;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;

public final class SystemUpdateBus {
    private static final LongProperty version = new SimpleLongProperty(0);

    private SystemUpdateBus() {
    }

    public static ReadOnlyLongProperty versionProperty() {
        return version;
    }

    public static void publish() {
        if (Platform.isFxApplicationThread()) {
            version.set(version.get() + 1);
        } else {
            Platform.runLater(() -> version.set(version.get() + 1));
        }
    }
}