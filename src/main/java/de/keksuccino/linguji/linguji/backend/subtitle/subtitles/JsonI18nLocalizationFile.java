package de.keksuccino.linguji.linguji.backend.subtitle.subtitles;

import com.google.gson.*;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.JsonI18nLocalizationLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class JsonI18nLocalizationFile extends AbstractSubtitle {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @Nullable
    public static JsonI18nLocalizationFile create(@NotNull File jsonFile) {
        if (!jsonFile.isFile()) return null;

        try {
            JsonI18nLocalizationFile localization = new JsonI18nLocalizationFile(jsonFile);
            if (localization.lines.isEmpty()) {
                LOGGER.error("Failed to parse JSON localization file!", new IllegalStateException("Lines list was empty!"));
                return null;
            }
            return localization;
        } catch (Exception ex) {
            LOGGER.error("Failed to create JsonLocalizationFile instance!", ex);
        }

        return null;
    }

    protected JsonI18nLocalizationFile(@NotNull File jsonFile) {
        this.parseJsonFile(Objects.requireNonNull(jsonFile));
    }

    protected void parseJsonFile(@NotNull File jsonFile) {
        try {
            // Explicitly use InputStreamReader with UTF-8 encoding for proper character handling
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);

                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        String key = entry.getKey();
                        if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                            String value = entry.getValue().getAsString();
                            this.lines.add(JsonI18nLocalizationLine.create(key, value));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to parse JSON localization file: " + jsonFile.getAbsolutePath(), ex);
        }
    }

    @Override
    public @NotNull String getFileExtension() {
        return "json";
    }

    @Override
    public @NotNull String serialize() {
        JsonObject jsonObject = new JsonObject();

        for (AbstractSubtitleLine line : this.lines) {
            if (line instanceof JsonI18nLocalizationLine entry) {
                String value = (entry.getTranslatedText() != null) ? entry.getTranslatedText() : entry.getTextWithoutFormattingCodes();
                value = value.replace(JsonI18nLocalizationLine.LINE_BREAK_UNIVERSAL, JsonI18nLocalizationLine.LINE_BREAK);
                jsonObject.addProperty(entry.getKey(), value);
            }
        }

        // Configure Gson to handle UTF-8 properly and preserve Unicode characters
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping() // Prevent escaping of non-ASCII characters
                .create();

        return gson.toJson(jsonObject);
    }
}