package de.keksuccino.linguji.linguji.backend.subtitle.subtitles;

import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.MathUtils;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.SrtTranslatableSubtitleLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SrtSubtitle extends AbstractSubtitle {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @Nullable
    public static SrtSubtitle create(@NotNull File srtFile) {

        if (!srtFile.isFile()) return null;

        try {
            SrtSubtitle subtitle = new SrtSubtitle(srtFile);
            if (subtitle.lines.isEmpty()) {
                LOGGER.error("Failed to parse SRT subtitle file!", new IllegalStateException("Lines list was empty!"));
                return null;
            }
            return subtitle;
        } catch (Exception ex) {
            LOGGER.error("Failed to create SrtSubtitle instance!", ex);
        }

        return null;

    }

    protected SrtSubtitle(@NotNull File srtFile) {
        this.parseSrtFile(Objects.requireNonNull(srtFile));
    }

    protected void parseSrtFile(@NotNull File srtFile) {

        int lineCounter = -1;
        boolean emptyLine = false;
        int currentSubtitleLineNumber = 0;
        String currentSubtitleLineTime = null;
        List<String> currentSubtitleLineContent = new ArrayList<>();

        for (String line : FileUtils.readTextLinesFrom(srtFile)) {

            lineCounter++;
            boolean subtitleLineNumberUpdated = false;

            //Cache subtitle line number if last line was empty OR it's the beginning of the file
            if ((emptyLine || (lineCounter == 0)) && MathUtils.isInteger(line.trim())) {
                currentSubtitleLineNumber++;
                subtitleLineNumberUpdated = true;
            }

            emptyLine = line.trim().isBlank();

            if (subtitleLineNumberUpdated) continue;

            if (currentSubtitleLineContent.isEmpty() && line.contains(" --> ")) {
                currentSubtitleLineTime = line;
                continue;
            }

            //End subtitle line on empty line
            if (emptyLine) {
                try {
                    if (!currentSubtitleLineContent.isEmpty() && (currentSubtitleLineTime != null) && (currentSubtitleLineNumber >= 1)) {
                        StringBuilder subLine = new StringBuilder();
                        boolean b = false;
                        for (String s : currentSubtitleLineContent) {
                            if (b) subLine.append("\n");
                            subLine.append(s);
                            b = true;
                        }
                        this.lines.add(SrtTranslatableSubtitleLine.create(subLine.toString(), currentSubtitleLineTime, currentSubtitleLineNumber));
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to parse line of SRT subtitle!", ex);
                }
                currentSubtitleLineContent.clear();
                currentSubtitleLineTime = null;
                continue;
            } else {
                currentSubtitleLineContent.add(line);
            }

        }

        try {
            if (!currentSubtitleLineContent.isEmpty() && (currentSubtitleLineTime != null) && (currentSubtitleLineNumber >= 1)) {
                StringBuilder subLine = new StringBuilder();
                boolean b = false;
                for (String s : currentSubtitleLineContent) {
                    if (b) subLine.append("\n");
                    subLine.append(s);
                    b = true;
                }
                this.lines.add(SrtTranslatableSubtitleLine.create(subLine.toString(), currentSubtitleLineTime, currentSubtitleLineNumber));
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to parse line of SRT subtitle!", ex);
        }

    }

    @Override
    public @NotNull String getFileExtension() {
        return "srt";
    }

    @Override
    public @NotNull String serialize() {

        StringBuilder s = new StringBuilder();

        for (AbstractSubtitleLine line : this.lines) {
            s.append(line.serialize()).append("\n\n");
        }

        return s.toString();

    }

}
