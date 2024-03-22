package de.keksuccino.linguji.linguji.backend.translator.deeplx;

import org.jetbrains.annotations.Nullable;

public class DeepLXResponse {

    public int code;
    public String data;

    @Nullable
    public String getText() {
        if (this.code == 200) return this.data;
        return null;
    }

}
