package edu.utech.helpdesk.client.util;

import edu.utech.helpdesk.client.ui.AlertUtil;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Runs blocking work (REST calls) off the JavaFX Application Thread so the GUI never freezes.
 * The success and failure callbacks are always delivered back on the JavaFX thread.
 */
public final class Async {

    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "helpdesk-worker");
        t.setDaemon(true);
        return t;
    });

    private Async() {
    }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onFailure.accept(task.getException()));
        POOL.execute(task);
    }

    /** Failures are shown to the user in an Alert. */
    public static <T> void run(Callable<T> work, Consumer<T> onSuccess) {
        run(work, onSuccess, AlertUtil::showError);
    }

    public static void shutdown() {
        POOL.shutdownNow();
    }
}
