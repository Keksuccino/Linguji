package de.keksuccino.polyglot.polyglot.backend.util;

import org.jetbrains.annotations.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {

    @NotNull
    protected final String url;
    protected final Map<String, String> header = new LinkedHashMap<>();

    @NotNull
    public static HttpRequest create(@NotNull String url) {
        return new HttpRequest(url);
    }

    public HttpRequest(@NotNull String url) {
        this.url = Objects.requireNonNull(url);
    }

    public HttpRequest addHeaderEntry(@NotNull String name, @NotNull String value) {
        this.header.put(name, value);
        return this;
    }

    @NotNull
    public Map<String, String> getHeader() {
        return this.header;
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "url='" + url + '\'' +
                ", header=" + header +
                '}';
    }

}
