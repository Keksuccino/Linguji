package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleViewMemory {

    public static final List<String> LOGGING_HISTORY = Collections.synchronizedList(new ArrayList<>());

    public static void init() {

        LogHandler.registerListener(LOGGING_HISTORY::add);

    }

}
