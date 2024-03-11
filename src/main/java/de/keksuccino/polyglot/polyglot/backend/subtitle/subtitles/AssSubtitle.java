package de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles;

import de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line.AssTranslatableSubtitleLine;
import de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line.SimpleSubtitleLine;
import de.keksuccino.polyglot.polyglot.backend.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.Objects;

public class AssSubtitle extends AbstractSubtitle {

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    protected String header = "";
    @NotNull
    protected String footer = "";

    @Nullable
    public static AssSubtitle create(@NotNull File assFile) {

        if (!assFile.isFile()) return null;

        try {
            AssSubtitle subtitle = new AssSubtitle(assFile);
            if (subtitle.header.trim().isEmpty() && subtitle.footer.trim().isEmpty()) {
                LOGGER.error("Failed to parse ASS subtitle file!", new IllegalStateException("Header and footer was empty!"));
                return null;
            }
            if (subtitle.lines.isEmpty()) {
                LOGGER.error("Failed to parse ASS subtitle file!", new IllegalStateException("Lines list was empty!"));
                return null;
            }
            return subtitle;
        } catch (Exception ex) {
            LOGGER.error("Failed to create AssSubtitle instance!", ex);
        }

        return null;

    }

    protected AssSubtitle(@NotNull File assFile) {
        this.parseAssFile(Objects.requireNonNull(assFile));
    }

    protected void parseAssFile(@NotNull File assFile) {

        boolean eventsSectionLineFound = false;
        boolean eventsSectionFormatLineFound = false;
        boolean eventsSectionFinished = false;

        StringBuilder headerTemp = new StringBuilder();
        StringBuilder footerTemp = new StringBuilder();

        for (String line : FileUtils.readTextLinesFrom(assFile)) {

//            //Remove UTF-8 BOM
//            if (line.startsWith(FileUtils.UTF8_BOM_CHAR)) {
//                line = line.substring(1);
//            }

            //Skip comments
            if (line.trim().startsWith("!") || line.trim().startsWith(";")) continue;

            //Parse header
            if (!eventsSectionFormatLineFound) {
                headerTemp.append(line).append("\n");
            }

            //End Events section
            if (eventsSectionFormatLineFound && line.trim().startsWith("[")) {
                eventsSectionFinished = true;
            }

            //Parse footer
            if (eventsSectionFinished) {
                footerTemp.append(line).append("\n");
            }

            if (!eventsSectionFinished) {

                //Parse Events section
                if (eventsSectionFormatLineFound) {
                    if (line.startsWith("Dialogue: ")) {
                        this.lines.add(AssTranslatableSubtitleLine.create(line));
                    } else if (!line.trim().isEmpty()) {
                        this.lines.add(new SimpleSubtitleLine(line));
                    }
                }

                //Search for start of Events section
                if (line.trim().equals("[Events]")) {
                    eventsSectionLineFound = true;
                }
                if (eventsSectionLineFound && line.trim().equals("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")) {
                    eventsSectionFormatLineFound = true;
                }

            }

        }

        this.header = headerTemp.toString();
        this.footer = footerTemp.toString();

    }

    @Override
    public @NotNull String getFileExtension() {
        return "ass";
    }

    @Override
    public @NotNull String serialize() {

        StringBuilder s = new StringBuilder();

        s.append(this.header);

        for (AbstractSubtitleLine line : this.lines) {
            s.append(line.serialize()).append("\n");
        }

        s.append(this.footer);

        return s.toString();

    }

}
