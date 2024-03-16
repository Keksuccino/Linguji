package de.keksuccino.linguji.linguji.backend.util.logger;

import de.keksuccino.linguji.linguji.backend.util.FileUtils;
import de.keksuccino.linguji.linguji.backend.util.ThreadUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class LogHandler {

    public static final File LOG_DIRECTORY = FileUtils.createDirectory(new File("logs"), false);
    public static final File SESSION_LOG_FILE = buildSessionLogFile();
    private static final List<Consumer<String>> LOG_LISTENERS = Collections.synchronizedList(new ArrayList<>());
    private static final List<String> LOG_FILE_MESSAGE_QUEUE = Collections.synchronizedList(new ArrayList<>());
    private static volatile boolean initialized = false;

    @NotNull
    public static SimpleLogger getLogger() {
        if (!initialized) {
            initialized = true;
            applyLoggerConfig();
            startFileWriterThread();
        }
        return new SimpleLogger(LogManager.getLogger(StackLocatorUtil.getCallerClass(3)));
    }

    protected static void onMessage(@NotNull String message) {
        new ArrayList<>(LOG_LISTENERS).forEach(stringConsumer -> {
            try {
                stringConsumer.accept(message);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        LOG_FILE_MESSAGE_QUEUE.add(message);
    }

    public static void registerListener(@NotNull Consumer<String> listener) {
        LOG_LISTENERS.add(Objects.requireNonNull(listener));
    }

    private static void startFileWriterThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    for (String s : new ArrayList<>(LOG_FILE_MESSAGE_QUEUE)) {
                        FileUtils.writeTextToFile(SESSION_LOG_FILE, true, s + "\n");
                        LOG_FILE_MESSAGE_QUEUE.remove(0);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ThreadUtils.sleep(50);
            }
        }, "LogHandler File Writer Thread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Makes Log4J not suck without having to mess with a frickin properties file.
     */
    private static void applyLoggerConfig() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.INFO);
        context.updateLoggers();
    }

    @NotNull
    private static File buildSessionLogFile() {
        Calendar c = Calendar.getInstance();
        String dateString = c.get(Calendar.DAY_OF_MONTH) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND);
        String fileName = "log_" + dateString + ".log";
        File f = FileUtils.generateUniqueFileName(new File(LOG_DIRECTORY, fileName), false);
        try {
            if (!f.isFile()) f.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return f;
    }

}
