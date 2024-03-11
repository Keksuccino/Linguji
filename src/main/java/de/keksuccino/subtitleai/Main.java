package de.keksuccino.subtitleai;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.NotNull;

public class Main {

    //TODO Threading auf maximal X Threads reduzieren (einstellbar), damit schneller übersetzt werden kann, ohne Minuten-Limit zu erreichen

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String VERSION = "1.0.0";

    private static Options options;

    public static void main(String[] args) {

        applyLoggerConfig();

        updateOptions();

        if (getOptions().geminiApiKey.getValue().trim().isEmpty()) {
            throw new RuntimeException("Invalid Gemini API key!");
        }

        try {

            Backend.translate();

        } catch (Exception ex) {
            ex.printStackTrace();
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