package de.keksuccino.linguji.linguji.backend.engine.engines.azure.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AzureRequest {

    public String to;
    public String from;
    public final String textType = "plain";
    public final String profanityAction;
    public boolean includeAlignment = true;

    public AzureRequest(@NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull ProfanityAction profanityAction) {
        this.profanityAction = profanityAction.name;
        this.to = targetLanguage;
        this.from = sourceLanguage;
    }

    public enum ProfanityAction {

        NO_ACTION("NoAction"),
        MARKED("Marked"),
        DELETED("Deleted");

        public final String name;

        ProfanityAction(String name) {
            this.name = name;
        }

        @Nullable
        public static ProfanityAction getByName(@NotNull String name) {
            for (ProfanityAction a : ProfanityAction.values()) {
                if (a.name.equals(name)) return a;
            }
            return null;
        }

    }

}
