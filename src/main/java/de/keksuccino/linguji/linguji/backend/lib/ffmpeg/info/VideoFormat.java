package de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info;

import java.util.Map;

public class VideoFormat {

    public String filename;
    public int nb_streams;
    public int nb_programs;
    public String format_name;
    public String format_long_name;
    public double start_time;
    public double duration;
    public long size;
    public long bit_rate;
    public int probe_score;
    public Map<String, String> tags;

}
