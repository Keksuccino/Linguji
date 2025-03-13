package de.keksuccino.linguji.linguji.backend.engine.engines.libretranslate;

import org.jetbrains.annotations.Nullable;

public class LibreTranslateRequest {

    /** The source text to translate. **/
    public String q;
    /** The source language. Use "auto" for auto language detection. **/
    public String source;
    /** The target language. **/
    public String target;
    public String format = "text";
    @Nullable
    public String api_key = null;

}
