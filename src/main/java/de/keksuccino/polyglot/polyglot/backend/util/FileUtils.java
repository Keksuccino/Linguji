package de.keksuccino.polyglot.polyglot.backend.util;

import de.keksuccino.polyglot.polyglot.backend.util.os.OSUtils;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileUtils {

    public static final String UTF8_BOM_CHAR = "\uFEFF";
    public static final boolean ON_OSX = OSUtils.isMacOS();

    public static void writeTextToFile(@NotNull File file, @NotNull String... textLines) {
        writeTextToFile(file, false, textLines);
    }

    public static void writeTextToFile(@NotNull File file, boolean append, @NotNull String... textLines) {
        BufferedWriter writer = null;
        try {
//            if (file.isFile()) {
//                Files.delete(file.toPath());
//            }
            writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING);
            if (textLines.length == 1) {
                writer.append(textLines[0]);
            } else {
                for (String line : textLines) {
                    writer.append(line);
                    writer.newLine();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        CloseableUtils.closeQuietly(writer);
    }

    public static void writeTextToFile(@NotNull File file, @NotNull List<String> textLines) {
        writeTextToFile(file, textLines.toArray(new String[0]));
    }

    public static void writeTextToFile(@NotNull File file, boolean append, @NotNull List<String> textLines) {
        writeTextToFile(file, append, textLines.toArray(new String[0]));
    }

    /** Reads all plain text lines from the given {@link InputStream}, closes it at the end and returns the text lines. **/
    @NotNull
    public static List<String> readTextLinesFrom(@NotNull InputStream in) {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        CloseableUtils.closeQuietly(reader);
        CloseableUtils.closeQuietly(in);
        return lines;
    }

    /** Reads all plain text lines from the given {@link File} and returns the text lines. **/
    @NotNull
    public static List<String> readTextLinesFrom(@NotNull File file) {
        try {
            return readTextLinesFrom(new FileInputStream(file));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    @NotNull
    public static File generateUniqueFileName(@NotNull File fileOrFolder, boolean isDirectory) {
        if (isDirectory && !fileOrFolder.isDirectory()) return fileOrFolder;
        if (!isDirectory && !fileOrFolder.isFile()) return fileOrFolder;
        File f = new File(fileOrFolder.getPath());
        int count = 1;
        while ((isDirectory && f.isDirectory()) || (!isDirectory && f.isFile())) {
            f = new File(fileOrFolder.getPath() + "_" + count);
            count++;
        }
        return f;
    }

    /**
     * Creates the given directory and returns it.
     */
    @NotNull
    public static File createDirectory(@NotNull File directory, boolean hideDirsWithDot) {
        try {
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (hideDirsWithDot && directory.getName().startsWith(".")) {
            try {
                Files.setAttribute(directory.toPath(), "dos:hidden", true);
            } catch (Exception ignore) {}
        }
        return directory;
    }

    public static void openFile(@NotNull File file) {
        try {
            String url = file.toURI().toURL().toString();
            String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            URL u = new URL(url);
            if (!ON_OSX) {
                if (s.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else {
                    if (u.getProtocol().equals("file")) {
                        url = url.replace("file:", "file://");
                    }
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                }
            } else {
                Runtime.getRuntime().exec(new String[]{"open", url});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
