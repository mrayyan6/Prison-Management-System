package com.prison.util;

import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;

public final class UiPerformanceUtil {
    private static final double DEFAULT_TABLE_ROW_HEIGHT = 32.0;

    private UiPerformanceUtil() {
    }

    // JavaFX renders with a retained scene graph, but caching heavy nodes provides a similar win.
    public static void enableBufferedRendering(Node... nodes) {
        if (nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node == null) {
                continue;
            }
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
        }
    }

    public static <T> void optimizeTableScrolling(TableView<T> tableView) {
        if (tableView == null) {
            return;
        }
        tableView.setFixedCellSize(DEFAULT_TABLE_ROW_HEIGHT);
        tableView.setCache(true);
        tableView.setCacheHint(CacheHint.SPEED);
    }

    public static void optimizeScrollPane(ScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setCache(true);
        scrollPane.setCacheHint(CacheHint.SPEED);
    }
}