package de.keksuccino.linguji.linguji.backend;

import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.Ffmpeg;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix.MkvToolNix;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import java.io.File;
import java.util.Objects;

public class Test {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) throws Exception {

        Ffmpeg ffmpeg = Ffmpeg.buildDefault();
        VideoInfo info = ffmpeg.getVideoInfo(new File("output.mkv"));

        for (VideoStream sub : info.getSubtitles()) {
            LOGGER.info("########## SUB #" + sub.index + ": " + sub);
        }

    }

}
