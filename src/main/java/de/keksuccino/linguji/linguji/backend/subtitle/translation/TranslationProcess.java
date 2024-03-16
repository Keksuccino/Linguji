package de.keksuccino.linguji.linguji.backend.subtitle.translation;

import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractTranslatableSubtitleLine;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslationProcess {

    public volatile boolean running = true;
    @Nullable
    public volatile List<AbstractSubtitle> subtitles;
    @Nullable
    public volatile AbstractSubtitle currentSubtitle;
    public final List<AbstractSubtitle> finishedSubtitles = Collections.synchronizedList(new ArrayList<>());
    public volatile int currentSubtitleTranslatableLinesCount = 0;
    public final List<AbstractTranslatableSubtitleLine> currentSubtitleFinishedLines = Collections.synchronizedList(new ArrayList<>());

    /**
     * Value between 0.0F and 1.0F.
     */
    public float getTotalProcess() {
        if (!this.running) return 0.0F;
        List<AbstractSubtitle> cachedSubtitles = this.subtitles;
        if (cachedSubtitles == null) cachedSubtitles = new ArrayList<>();
        int cachedFinishedSubtitles = this.finishedSubtitles.size();
        if (!cachedSubtitles.isEmpty() && (cachedFinishedSubtitles > 0)) {
            return Math.min(1.0F, Math.max(0.0F, (float)cachedFinishedSubtitles / (float)cachedSubtitles.size()));
        }
        return 0.0F;
    }

    /**
     * Value between 0.0F and 1.0F.
     */
    public float getCurrentSubtitleProcess() {
        if (!this.running) return 0.0F;
        AbstractSubtitle cachedCurrent = this.currentSubtitle;
        int cachedCount = this.currentSubtitleTranslatableLinesCount;
        int cachedFinishedLines = this.currentSubtitleFinishedLines.size();
        if ((cachedCurrent != null) && (cachedFinishedLines > 0)) {
            return Math.min(1.0F, Math.max(0.0F, (float)cachedFinishedLines / (float)cachedCount));
        }
        return 0.0F;
    }

}
