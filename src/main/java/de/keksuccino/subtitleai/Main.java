package de.keksuccino.subtitleai;

import de.keksuccino.subtitleai.ai.gemini.GeminiTranslator;
import de.keksuccino.subtitleai.subtitle.subtitles.AssSubtitle;
import de.keksuccino.subtitleai.subtitle.translation.SubtitleTranslator;
import de.keksuccino.subtitleai.util.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.Objects;

public class Main {

    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!
    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!
    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!
    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!
    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!
    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!

    //TODO Schauen, warum Umlaute trotz UTF-8 nicht richtig in File geschrieben werden

    private static final Logger LOGGER = LogManager.getLogger();

    private static Options options;

    public static void main(String[] args) {

        applyLoggerConfig();

        updateOptions();

        if (getOptions().geminiApiKey.getValue().trim().isEmpty()) {
            throw new RuntimeException("Invalid Gemini API key!");
        }

        GeminiTranslator aiTranslator = new GeminiTranslator(getOptions().geminiApiKey.getValue());
        SubtitleTranslator<AssSubtitle> assTranslator = new SubtitleTranslator<>(aiTranslator);
        AssSubtitle subtitle = Objects.requireNonNull(AssSubtitle.create(new File("subtitle.ass")));

        try {

            assTranslator.translate(subtitle, "English", "German");
            FileUtils.writeTextToFile(new File("subtitle_translated.ass"), false, subtitle.serialize());

        } catch (Exception ex) {
            LOGGER.error("Failed to translate ASS file!", ex);
        }

    }

    /**
     * Makes Log4J not suck without having to mess with a frickin properties file. (who tf set the default level to ERROR ?!)
     */
    private static void applyLoggerConfig() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.INFO);
        context.updateLoggers();
    }

    public static void updateOptions() {
        options = new Options();
    }

    @NotNull
    public static Options getOptions() {
        if (options == null) updateOptions();
        return options;
    }

}