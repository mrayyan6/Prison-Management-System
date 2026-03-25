package com.prison.util;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BackgroundLoader {
    private BackgroundLoader() {
    }

    public static <T> void loadAsync(Supplier<T> supplier, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return supplier.get();
            }
        };

        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            if (onError != null) {
                onError.accept(task.getException());
            }
        });

        Thread worker = new Thread(task, "pms-background-loader");
        worker.setDaemon(true);
        worker.start();
    }
}