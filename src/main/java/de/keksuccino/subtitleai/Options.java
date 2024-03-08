package de.keksuccino.subtitleai;

import de.keksuccino.subtitleai.util.config.Config;
import de.keksuccino.subtitleai.util.options.AbstractOptions;

public class Options extends AbstractOptions {

    protected final Config config = new Config("config.txt");

    public final Option<String> geminiApiKey = new Option<>(config, "gemini_api_key", "", "general");

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
