module de.keksuccino.linguji.linguji {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.jetbrains.annotations;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.google.gson;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.core5.httpcore5.h2;
    requires com.google.common;
    requires org.apache.commons.lang3;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires javafx.graphics;

    exports de.keksuccino.linguji.linguji.frontend;
    exports de.keksuccino.linguji.linguji.backend;
    exports de.keksuccino.linguji.linguji.backend.translator;
    exports de.keksuccino.linguji.linguji.backend.translator.gemini;
    exports de.keksuccino.linguji.linguji.backend.translator.gemini.response;
    exports de.keksuccino.linguji.linguji.backend.translator.gemini.request;
    exports de.keksuccino.linguji.linguji.backend.translator.gemini.safety;
    exports de.keksuccino.linguji.linguji.backend.translator.gemini.exceptions;
    exports de.keksuccino.linguji.linguji.backend.util;
    exports de.keksuccino.linguji.linguji.backend.util.os;
    exports de.keksuccino.linguji.linguji.backend.util.options;
    exports de.keksuccino.linguji.linguji.backend.util.config;
    exports de.keksuccino.linguji.linguji.backend.util.config.exceptions;
    exports de.keksuccino.linguji.linguji.backend.subtitle.subtitles;
    exports de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;
    exports de.keksuccino.linguji.linguji.backend.subtitle.translation;
    exports de.keksuccino.linguji.linguji.backend.util.logger;
    exports de.keksuccino.linguji.linguji.frontend.util;
    exports de.keksuccino.linguji.linguji.frontend.util.os.windows;

    opens de.keksuccino.linguji.linguji.frontend;
    opens de.keksuccino.linguji.linguji.backend;
    opens de.keksuccino.linguji.linguji.backend.translator;
    opens de.keksuccino.linguji.linguji.backend.translator.gemini;
    opens de.keksuccino.linguji.linguji.backend.translator.gemini.response;
    opens de.keksuccino.linguji.linguji.backend.translator.gemini.request;
    opens de.keksuccino.linguji.linguji.backend.translator.gemini.safety;
    opens de.keksuccino.linguji.linguji.backend.translator.gemini.exceptions;
    opens de.keksuccino.linguji.linguji.backend.util;
    opens de.keksuccino.linguji.linguji.backend.util.os;
    opens de.keksuccino.linguji.linguji.backend.util.options;
    opens de.keksuccino.linguji.linguji.backend.util.config;
    opens de.keksuccino.linguji.linguji.backend.util.config.exceptions;
    opens de.keksuccino.linguji.linguji.backend.subtitle.subtitles;
    opens de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;
    opens de.keksuccino.linguji.linguji.backend.subtitle.translation;
    opens de.keksuccino.linguji.linguji.frontend.util;
    opens de.keksuccino.linguji.linguji.backend.util.logger;
    opens de.keksuccino.linguji.linguji.frontend.util.os.windows;

}