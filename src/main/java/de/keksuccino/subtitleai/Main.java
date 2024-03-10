package de.keksuccino.subtitleai;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.NotNull;

public class Main {

    //TODO Timeouts abfangen und in dem Fall einfach nochmal probieren: https://gist.github.com/Keksuccino/4bd978ee5b807c73a0faee908afd9b46

    //TODO single-line translation implementieren (target ist immer eine Line, aber als Kontext wird vorherige und nachfolgende mitgesendet) ---> NUR FALLS NORMALER MODE FAILT !!!!

    //TODO toggleable option: wenn X mal soft-block oder hard-block , alle profanity settings auf höchste Tolleranz stellen und Abschnitt erneut versuchen

    //TODO Threading auf maximal X Threads reduzieren (einstellbar), damit schneller übersetzt werden kann, ohne Minuten-Limit zu erreichen

    private static final Logger LOGGER = LogManager.getLogger();

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