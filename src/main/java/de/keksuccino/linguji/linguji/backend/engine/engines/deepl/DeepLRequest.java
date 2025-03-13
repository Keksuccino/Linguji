package de.keksuccino.linguji.linguji.backend.engine.engines.deepl;

import org.jetbrains.annotations.NotNull;

public class DeepLRequest {

    public String[] text;
    public String target_lang;

    public DeepLRequest(@NotNull String target_lang, @NotNull String... text) {
        this.text = text;
        this.target_lang = target_lang;
    }

}
