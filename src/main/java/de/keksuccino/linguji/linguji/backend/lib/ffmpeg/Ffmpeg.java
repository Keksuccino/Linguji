package de.keksuccino.linguji.linguji.backend.lib.ffmpeg;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.lib.CliExecutor;
import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ffmpeg extends CliExecutor {

    public static final File DEFAULT_FFMPEG_DIRECTORY = new File("ffmpeg");

    protected static final String FFPROBE_PATH_PLACEHOLDER = "%ffprobe_path%";
    protected static final String FFMPEG_PATH_PLACEHOLDER = "%ffmpeg_path%";
    protected static final String VIDEO_FILE_PATH = "%video_path%";
    protected static final String VIDEO_INFO_COMMAND = "\"%ffprobe_path%\" -v quiet -print_format json -show_format -show_streams \"%video_path%\"";

    @NotNull
    protected final File ffmpegDirectory;
    @NotNull
    protected final File ffmpegExecutable;
    @NotNull
    protected final File ffprobeExecutable;

    @NotNull
    public static Ffmpeg buildDefault() throws FileNotFoundException {
        FileUtils.createDirectory(DEFAULT_FFMPEG_DIRECTORY, false);
        return new Ffmpeg(DEFAULT_FFMPEG_DIRECTORY);
    }

    public Ffmpeg(@NotNull File ffmpegDirectory) throws FileNotFoundException {
        this.ffmpegDirectory = ffmpegDirectory;
        this.ffmpegExecutable = new File(this.ffmpegDirectory, "ffmpeg.exe");
        if (!this.ffmpegExecutable.isFile()) throw new FileNotFoundException("ffmpeg executable not found: " + this.ffmpegExecutable.getAbsolutePath());
        this.ffprobeExecutable = new File(this.ffmpegDirectory, "ffprobe.exe");
        if (!this.ffprobeExecutable.isFile()) throw new FileNotFoundException("ffprobe executable not found: " + this.ffprobeExecutable.getAbsolutePath());
    }

    @NotNull
    public VideoInfo getVideoInfo(@NotNull File videoFile) throws Exception {

        if (!videoFile.isFile()) throw new FileNotFoundException("video file not found: " + videoFile.getAbsolutePath());

        Gson gson = new Gson();

        List<String> args = new ArrayList<>();
        args.add("\"" + this.ffprobeExecutable.getAbsolutePath() + "\"");
        args.add("-v");
        args.add("quiet");
        args.add("-print_format");
        args.add("json");
        args.add("-show_format");
        args.add("-show_streams");
        args.add("\"" + videoFile.getAbsolutePath() + "\"");

        String feedback = this.execute(args.toArray(new String[0]));

        return Objects.requireNonNull(gson.fromJson(feedback, VideoInfo.class));

    }

}
