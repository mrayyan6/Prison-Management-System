package com.prison.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;

public final class AutoReleaseService {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auto-release-service");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean started;

    private AutoReleaseService() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        started = true;

        // Run once at startup so first login reflects immediate releases.
        checkDailyReleases();

        scheduler.scheduleAtFixedRate(AutoReleaseService::checkDailyReleases, 24, 24, TimeUnit.HOURS);
    }

    public static void checkDailyReleases() {
        List<String> releasedNames = Database.getInstance().autoReleaseDueInmates();
        if (!releasedNames.isEmpty()) {
            for (String name : releasedNames) {
                ActivityLogService.log("Auto Release", "Inmate " + name + " Auto-Released (Term Completed)");
            }
            SystemUpdateBus.publish();
        }
    }
}