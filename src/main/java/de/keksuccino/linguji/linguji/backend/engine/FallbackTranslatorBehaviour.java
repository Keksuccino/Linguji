package de.keksuccino.linguji.linguji.backend.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum FallbackTranslatorBehaviour {

    DONT_USE_FALLBACK("dont_use_fallback", "Don't Use Fallback"),
    TRANSLATE_FULL_PACKET("translate_full_packet", "Translate Full Packet"),
    TRY_TRANSLATE_BAD_PARTS_OF_PACKET("try_translate_bad_parts_of_packet", "Try Translate Bad Parts Of Packet");

    @NotNull
    private final String displayName;
    private final String name;

    FallbackTranslatorBehaviour(@NotNull String name, @NotNull String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public static FallbackTranslatorBehaviour getByName(@NotNull String name) {
        for (FallbackTranslatorBehaviour s : FallbackTranslatorBehaviour.values()) {
            if (s.name.equals(name)) return s;
        }
        return null;
    }

    @Nullable
    public static FallbackTranslatorBehaviour getByDisplayName(@NotNull String displayName) {
        for (FallbackTranslatorBehaviour s : FallbackTranslatorBehaviour.values()) {
            if (s.displayName.equals(displayName)) return s;
        }
        return null;
    }

}
