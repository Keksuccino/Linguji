package de.keksuccino.linguji.linguji.backend.util;

public class ThreadUtils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignore) {}
    }

}