package de.keksuccino.linguji.linguji.backend.lib.logger;

import de.keksuccino.linguji.linguji.backend.lib.DateUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class SimpleLogger {

    @NotNull
    protected final Logger logger;
    @NotNull
    protected String pattern = "[%date%] [%level%] %msg%";

    protected SimpleLogger(@NotNull Logger logger) {
        this.logger = logger;
    }

    public void info(@NotNull String message) {
        this.logger.info(message);
        LogHandler.onMessage(this.formatMessage(message, "INFO"));
    }

    public void info(@NotNull String message, @NotNull Throwable throwable) {
        this.logger.info(message, throwable);
        LogHandler.onMessage(this.formatMessage(this.appendThrowable(message, throwable), "INFO"));
    }

    public void warn(@NotNull String message) {
        this.logger.warn(message);
        LogHandler.onMessage(this.formatMessage(message, "WARN"));
    }

    public void warn(@NotNull String message, @NotNull Throwable throwable) {
        this.logger.warn(message, throwable);
        LogHandler.onMessage(this.formatMessage(this.appendThrowable(message, throwable), "WARN"));
    }

    public void error(@NotNull String message) {
        this.logger.error(message);
        LogHandler.onMessage(this.formatMessage(message, "ERROR"));
    }

    public void error(@NotNull String message, @NotNull Throwable throwable) {
        this.logger.error(message, throwable);
        LogHandler.onMessage(this.formatMessage(this.appendThrowable(message, throwable), "ERROR"));
    }

    @NotNull
    protected String appendThrowable(@NotNull String message, @NotNull Throwable throwable) {
        StringBuilder finalMsg = new StringBuilder(message);
        finalMsg.append("\n").append(throwable.toString());
        for (StackTraceElement traceElement : throwable.getStackTrace()) {
            finalMsg.append("\n").append("\tat ").append(traceElement.toString());
        }
        return finalMsg.toString();
    }

    @NotNull
    protected String formatMessage(@NotNull String message, @NotNull String level) {
        return this.getPattern().replace("%date%", this.getCurrentDateTimeString()).replace("%level%", level).replace("%msg%", message);
    }

    @NotNull
    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(@NotNull String pattern) {
        this.pattern = Objects.requireNonNull(pattern);
    }

    protected String getCurrentDateTimeString() {
        return DateUtils.getDateTimeString();
    }

}
