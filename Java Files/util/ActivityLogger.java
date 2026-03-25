package com.prison.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ActivityLogger {
    private static final DateTimeFormatter FEED_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy | hh:mm a");

    private ActivityLogger() {
    }

    public static void log(String action, String details) {
        String formattedTime = LocalDateTime.now().format(FEED_TIME_FORMAT);
        Database.getInstance().addActivityLog(formattedTime, action, details);
        SystemUpdateBus.publish();
    }
}