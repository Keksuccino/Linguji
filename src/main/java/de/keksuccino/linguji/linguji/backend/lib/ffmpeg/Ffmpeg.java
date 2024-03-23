package de.keksuccino.linguji.linguji.backend.lib.ffmpeg;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.lib.CloseableCollector;
import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Ffmpeg {

    //info befehl cached: ffprobe -v quiet -print_format json -show_format -show_streams "output.mkv"
    //funktionierender sub add befehl (entfernt attachments/fonts): ffmpeg -i kubo03.mkv -i kubo03.ass -map 0:v -map 0:a -map 1:s -map 0:s -disposition:s:0 default -c:v copy -c:a copy -c:s copy output.mkv
    //add sub befehl, der alle streams (auch attachments) kopiert, allerdings ist neuer ASS sub dann broken: ffmpeg -i kubo03.mkv -i kubo03.ass -map 0 -map 1 -c:v copy -c:a copy -c:s copy output.mkv

    protected static final File DEFAULT_FFMPEG_DIRECTORY = new File("ffmpeg");

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
        if (!this.ffmpegExecutable.isFile()) throw new FileNotFoundException("ffmpeg executable not found!");
        this.ffprobeExecutable = new File(this.ffmpegDirectory, "ffprobe.exe");
        if (!this.ffprobeExecutable.isFile()) throw new FileNotFoundException("ffprobe executable not found!");
    }

    @NotNull
    public VideoInfo getVideoInfo(@NotNull File videoFile) throws Exception {

        if (!videoFile.isFile()) throw new FileNotFoundException("video file not found!");

        Gson gson = new Gson();

        String command = VIDEO_INFO_COMMAND.replace(FFPROBE_PATH_PLACEHOLDER, this.ffprobeExecutable.getAbsolutePath().replace("\\", "/")).replace(VIDEO_FILE_PATH, videoFile.getAbsolutePath().replace("\\", "/"));
        return Objects.requireNonNull(gson.fromJson(execute(command), VideoInfo.class));

    }

    @NotNull
    protected static String execute(@NotNull String... command) throws Exception {
        CloseableCollector collector = CloseableCollector.create();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true); // Combine standard error with standard output
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = collector.put(new BufferedReader(collector.put(new InputStreamReader(collector.put(process.getInputStream())))));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            collector.closeQuietly();
            return output.toString();
        } catch (Exception ex) {
            collector.closeQuietly();
            throw ex;
        }
    }

}
