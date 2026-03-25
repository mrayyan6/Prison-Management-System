package com.prison.util;

public final class ActivityLogService {
    private ActivityLogService() {
    }

    public static void log(String action, String details) {
        ActivityLogger.log(action, details);
    }
}