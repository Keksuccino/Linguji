module de.keksuccino.polyglot.polyglot {

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

    exports de.keksuccino.polyglot.polyglot.frontend;
    exports de.keksuccino.polyglot.polyglot.backend;
    exports de.keksuccino.polyglot.polyglot.backend.translator;
    exports de.keksuccino.polyglot.polyglot.backend.translator.gemini;
    exports de.keksuccino.polyglot.polyglot.backend.translator.gemini.response;
    exports de.keksuccino.polyglot.polyglot.backend.translator.gemini.request;
    exports de.keksuccino.polyglot.polyglot.backend.translator.gemini.safety;
    exports de.keksuccino.polyglot.polyglot.backend.translator.gemini.exceptions;
    exports de.keksuccino.polyglot.polyglot.backend.util;
    exports de.keksuccino.polyglot.polyglot.backend.util.os;
    exports de.keksuccino.polyglot.polyglot.backend.util.options;
    exports de.keksuccino.polyglot.polyglot.backend.util.config;
    exports de.keksuccino.polyglot.polyglot.backend.util.config.exceptions;
    exports de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles;
    exports de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line;
    exports de.keksuccino.polyglot.polyglot.backend.subtitle.translation;
    exports de.keksuccino.polyglot.polyglot.backend.util.logger;
    exports de.keksuccino.polyglot.polyglot.frontend.util;
    exports de.keksuccino.polyglot.polyglot.frontend.util.os.windows;

    opens de.keksuccino.polyglot.polyglot.frontend;
    opens de.keksuccino.polyglot.polyglot.backend;
    opens de.keksuccino.polyglot.polyglot.backend.translator;
    opens de.keksuccino.polyglot.polyglot.backend.translator.gemini;
    opens de.keksuccino.polyglot.polyglot.backend.translator.gemini.response;
    opens de.keksuccino.polyglot.polyglot.backend.translator.gemini.request;
    opens de.keksuccino.polyglot.polyglot.backend.translator.gemini.safety;
    opens de.keksuccino.polyglot.polyglot.backend.translator.gemini.exceptions;
    opens de.keksuccino.polyglot.polyglot.backend.util;
    opens de.keksuccino.polyglot.polyglot.backend.util.os;
    opens de.keksuccino.polyglot.polyglot.backend.util.options;
    opens de.keksuccino.polyglot.polyglot.backend.util.config;
    opens de.keksuccino.polyglot.polyglot.backend.util.config.exceptions;
    opens de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles;
    opens de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line;
    opens de.keksuccino.polyglot.polyglot.backend.subtitle.translation;
    opens de.keksuccino.polyglot.polyglot.frontend.util;
    opens de.keksuccino.polyglot.polyglot.backend.util.logger;
    opens de.keksuccino.polyglot.polyglot.frontend.util.os.windows;

}