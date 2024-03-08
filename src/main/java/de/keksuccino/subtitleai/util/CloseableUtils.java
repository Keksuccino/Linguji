package de.keksuccino.subtitleai.util;

import org.jetbrains.annotations.Nullable;

public class CloseableUtils {

    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignore) {}
    }

}
