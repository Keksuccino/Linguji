package de.keksuccino.linguji.linguji.backend.lib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CloseableCollector implements Closeable {

    protected final List<Closeable> closeables = new ArrayList<>();

    @NotNull
    public static CloseableCollector create() {
        return new CloseableCollector();
    }

    public <T extends Closeable> T put(@Nullable T closeable) {
        if (closeable == null) return null;
        this.closeables.add(closeable);
        return closeable;
    }

    public void forEach(Consumer<Closeable> closeableConsumer) {
        for (Closeable c : this.closeables) {
            closeableConsumer.accept(c);
        }
    }

    /**
     * Tries to close all {@link Closeable}s.<br>
     * Can fail to close all {@link Closeable}s if one throws an exception!
     * For better exception handling, use {@link CloseableCollector#forEach(Consumer)}.
     */
    @Override
    public void close() throws IOException {
        for (Closeable c : this.closeables) {
            c.close();
        }
    }

    public void closeQuietly() {
        for (Closeable c : this.closeables) {
            CloseableUtils.closeQuietly(c);
        }
    }

}
