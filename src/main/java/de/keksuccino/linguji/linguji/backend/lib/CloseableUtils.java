package de.keksuccino.linguji.linguji.backend.lib;

import org.jetbrains.annotations.Nullable;

public class CloseableUtils {

    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignore) {}
    }

}
