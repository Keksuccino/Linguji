package de.keksuccino.linguji.linguji.backend.util.os;

import java.util.Locale;

public class OSUtils {

    private static OperatingSystemType cachedOsType;

    public static boolean isWindows() {
        return getOperatingSystemType() == OperatingSystemType.WINDOWS;
    }

    public static boolean isMacOS() {
        return getOperatingSystemType() == OperatingSystemType.MACOS;
    }

    public static boolean isLinux() {
        return getOperatingSystemType() == OperatingSystemType.LINUX;
    }

    public static OperatingSystemType getOperatingSystemType() {
        if (cachedOsType == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                cachedOsType = OperatingSystemType.MACOS;
            } else if (OS.contains("win")) {
                cachedOsType = OperatingSystemType.WINDOWS;
            } else if (OS.contains("nux")) {
                cachedOsType = OperatingSystemType.LINUX;
            } else {
                cachedOsType = OperatingSystemType.OTHER;
            }
        }
        return cachedOsType;
    }

}
