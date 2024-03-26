package de.keksuccino.linguji.linguji.frontend;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskExecutor {

    private static final List<Runnable> TASKS = Collections.synchronizedList(new ArrayList<>());

    public static void queueTask(@NotNull Runnable task) {
        TASKS.add(task);
    }

    public static void runQueuedTasks() {
        if (TASKS.isEmpty()) return;
        List<Runnable> queuedTasks = new ArrayList<>(TASKS);
        queuedTasks.forEach(runnable -> {
            runnable.run();
            TASKS.remove(runnable);
        });
    }

}
