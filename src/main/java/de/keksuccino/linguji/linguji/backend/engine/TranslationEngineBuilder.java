package de.keksuccino.linguji.linguji.backend.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class TranslationEngineBuilder<T extends AbstractTranslationEngine> {

    protected Supplier<T> builderSupplier;
    protected Supplier<Boolean> readySupplier;
    protected String name;
    protected String displayName;

    public TranslationEngineBuilder(@NotNull Supplier<T> builderSupplier, @NotNull Supplier<Boolean> readySupplier, @NotNull String name, @NotNull String displayName) {
        this.builderSupplier = Objects.requireNonNull(builderSupplier);
        this.readySupplier = Objects.requireNonNull(readySupplier);
        this.name = Objects.requireNonNull(name);
        this.displayName = Objects.requireNonNull(displayName);
    }

    @Nullable
    public T createInstance() {
        if (!this.isTranslatorReady()) return null;
        return this.builderSupplier.get();
    }

    public boolean isTranslatorReady() {
        if (SharedTranslatorOptions.getSourceLanguage() == null) return false;
        if (SharedTranslatorOptions.getTargetLanguage() == null) return false;
        return this.readySupplier.get();
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

}
