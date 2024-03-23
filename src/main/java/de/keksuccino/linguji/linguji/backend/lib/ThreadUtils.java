package de.keksuccino.linguji.linguji.backend.lib;

public class ThreadUtils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignore) {}
    }

}
