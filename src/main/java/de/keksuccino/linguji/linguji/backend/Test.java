package de.keksuccino.linguji.linguji.backend;

import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.libretranslate.LibreTranslationEngine;
import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;

public class Test {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) throws Exception {

        LibreTranslationEngine engine = new LibreTranslationEngine(Backend.getOptions().libreTranslateUrl.getValue(), Backend.getOptions().libreTranslateApiKey.getValue());

        LOGGER.info("######################### TRANSLATED: " + engine.translate("This is a simple text.\nI want to check how well the engine can translate it.", "en", "de", new TranslationProcess()));

    }

}
