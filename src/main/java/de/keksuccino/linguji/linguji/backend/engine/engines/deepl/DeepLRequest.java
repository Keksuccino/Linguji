package de.keksuccino.linguji.linguji.backend.engine.engines.deepl;

import org.jetbrains.annotations.NotNull;

public class DeepLRequest {

    public String[] text;
    public String source_lang;
    public String target_lang;

    public DeepLRequest(@NotNull String source_lang, @NotNull String target_lang, @NotNull String... text) {
        this.text = text;
        this.source_lang = source_lang;
        this.target_lang = target_lang;
    }

}
