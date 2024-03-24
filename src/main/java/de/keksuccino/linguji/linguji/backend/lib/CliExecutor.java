package de.keksuccino.linguji.linguji.backend.lib;

import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class CliExecutor {

    @NotNull
    protected String execute(@NotNull String... command) throws Exception {
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
