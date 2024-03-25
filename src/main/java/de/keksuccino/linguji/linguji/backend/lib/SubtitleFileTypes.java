package de.keksuccino.linguji.linguji.backend.lib;

import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubtitleFileTypes {

    @Nullable
    public static String getFileExtension(@NotNull VideoStream subtitleTrack) {
        if (subtitleTrack.isSubtitle()) {
            //ASS
            if ("ass".equals(subtitleTrack.getSubtitleType())) return "ass";
        }
        return null;
    }

}
