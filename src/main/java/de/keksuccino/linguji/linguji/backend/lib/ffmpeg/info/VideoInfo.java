package de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoInfo {

    public VideoStream[] streams;
    public VideoFormat format;

    @NotNull
    public List<VideoStream> getStreams() {
        List<VideoStream> streams = new ArrayList<>();
        if (this.streams != null) streams = new ArrayList<>(Arrays.asList(this.streams));
        return streams;
    }

    @NotNull
    public List<VideoStream> getSubtitles() {
        List<VideoStream> subtitles = new ArrayList<>();
        if (this.streams != null) {
            for (VideoStream stream : this.streams) {
                if (stream.isSubtitle()) subtitles.add(stream);
            }
        }
        return subtitles;
    }

    @NotNull
    public List<VideoStream> getSubtitlesOfType(@NotNull String subtitleType) {
        List<VideoStream> subtitles = this.getSubtitles();
        subtitles.removeIf(videoStream -> (videoStream.getSubtitleType() == null) || !subtitleType.equals(videoStream.getSubtitleType()));
        return subtitles;
    }

}
