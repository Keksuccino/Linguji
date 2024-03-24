package de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix;

import de.keksuccino.linguji.linguji.backend.lib.CliExecutor;
import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MkvToolNix extends CliExecutor {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static final File DEFAULT_MKVTOOLNIX_DIRECTORY = new File("mkvtoolnix");

    @NotNull
    protected File mkvToolNixDirectory;
    @NotNull
    protected File mkvmergeExecutable;

    @NotNull
    public static MkvToolNix createDefault() throws FileNotFoundException {
        return new MkvToolNix(DEFAULT_MKVTOOLNIX_DIRECTORY);
    }

    public MkvToolNix(@NotNull File mkvToolNixDirectory) throws FileNotFoundException {
        this.mkvToolNixDirectory = Objects.requireNonNull(mkvToolNixDirectory);
        FileUtils.createDirectory(this.mkvToolNixDirectory, false);
        this.mkvmergeExecutable = new File(this.mkvToolNixDirectory, "mkvmerge.exe");
        if (!this.mkvmergeExecutable.isFile()) throw new FileNotFoundException("mkvmerge executable not found: " + this.mkvmergeExecutable.getAbsolutePath());
    }

    public void addSubtitleToMkv(@NotNull File mkvFileIn, @NotNull File mkvFileOut, @NotNull File subtitleFile, @Nullable Locale subtitleLang, boolean setSubtitleAsDefault) throws Exception {

        if (!mkvFileIn.isFile()) throw new FileNotFoundException("Input MKV file not found: " + mkvFileIn.getAbsolutePath());
        if (!subtitleFile.isFile()) throw new FileNotFoundException("Input subtitle file not found: " + subtitleFile.getAbsolutePath());

        List<String> args = new ArrayList<>();
        args.add("\"" + this.mkvmergeExecutable.getAbsolutePath() + "\"");
        args.add("-o");
        args.add("\"" + mkvFileOut.getAbsolutePath() + "\"");
        args.add("\"" + mkvFileIn.getAbsolutePath() + "\"");
        if (subtitleLang != null) {
            args.add("--language");
            args.add("0:" + subtitleLang.getIso());
        }
        args.add("--default-track-flag");
        args.add("0:" + (setSubtitleAsDefault ? "1" : "0"));
        args.add("\"" + subtitleFile.getAbsolutePath() + "\"");
        args.add("--gui-mode");

        String feedback = this.execute(args.toArray(new String[0]));

        LOGGER.info("MKVTOOLNIX FEEDBACK: \n" + feedback);

        for (String line : feedback.lines().toList()) {
            if (line.startsWith("#GUI#error")) throw new MkvToolNixException(line);
        }

    }

}
