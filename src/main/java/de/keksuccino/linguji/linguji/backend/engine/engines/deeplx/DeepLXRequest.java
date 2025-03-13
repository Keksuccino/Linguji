package de.keksuccino.linguji.linguji.backend.engine.engines.deeplx;

import org.jetbrains.annotations.NotNull;

public class DeepLXRequest {

    public String text;
    public String source_lang;
    public String target_lang;

    public DeepLXRequest(@NotNull String text, @NotNull String source_lang, @NotNull String target_lang) {
        this.text = text;
        this.source_lang = source_lang;
        this.target_lang = target_lang;
    }

}
