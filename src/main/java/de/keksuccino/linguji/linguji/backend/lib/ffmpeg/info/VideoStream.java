package de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public class VideoStream {

    public int index;
    public String codec_name;
    public String codec_long_name;
    public String profile;
    public String codec_type;
    public int width;
    public int height;
    public int codec_width;
    public int codec_height;
    public Map<String, Integer> disposition;
    public Map<String, String> tags;

    public boolean isDefault() {
        return this.isDispositionFlag("default");
    }

    public boolean isDispositionFlag(@NotNull String flag) {
        if (this.disposition == null) return false;
        if (!this.disposition.containsKey(flag)) return false;
        return (this.disposition.get(flag) == 1);
    }

    @Nullable
    public String getSubtitleType() {
        if (!this.isSubtitle()) return null;
        return this.codec_name;
    }

    @Nullable
    public String getLanguage() {
        return this.getTag("language");
    }

    @Nullable
    public String getTag(@NotNull String key) {
        if (this.tags == null) return null;
        return this.tags.get(key);
    }

    public boolean isSubtitle() {
        return "subtitle".equals(this.codec_type);
    }

    public boolean isVideo() {
        return "video".equals(this.codec_type);
    }

    public boolean isAudio() {
        return "audio".equals(this.codec_type);
    }

    @Override
    public String toString() {
        return "VideoStream{" +
                "index=" + index +
                ", codec_name='" + codec_name + '\'' +
                ", codec_long_name='" + codec_long_name + '\'' +
                ", profile='" + profile + '\'' +
                ", codec_type='" + codec_type + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", codec_width=" + codec_width +
                ", codec_height=" + codec_height +
                ", disposition=" + disposition +
                ", tags=" + tags +
                '}';
    }

}
