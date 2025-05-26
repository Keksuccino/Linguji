package de.keksuccino.linguji.linguji.backend.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class JsonUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    public static JsonArray toStringJsonArray(@NotNull List<String> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    @NotNull
    public static JsonArray toStringJsonArray(@NotNull String[] array) {
        return toStringJsonArray(Arrays.asList(array));
    }

    @NotNull
    public static JsonArray toBooleanJsonArray(@NotNull List<Boolean> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    @NotNull
    public static JsonArray toBooleanJsonArray(@NotNull Boolean[] array) {
        return toBooleanJsonArray(Arrays.asList(array));
    }

    @NotNull
    public static JsonArray toNumberJsonArray(@NotNull List<Number> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    @NotNull
    public static JsonArray toNumberJsonArray(@NotNull Number[] array) {
        return toNumberJsonArray(Arrays.asList(array));
    }

    @NotNull
    public static JsonArray toCharacterJsonArray(@NotNull List<Character> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    @NotNull
    public static JsonArray toCharacterJsonArray(@NotNull Character[] array) {
        return toCharacterJsonArray(Arrays.asList(array));
    }

    @NotNull
    public static <T extends JsonElement> JsonArray toElementJsonArray(@NotNull List<T> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    @NotNull
    public static <T extends JsonElement> JsonArray toElementJsonArray(@NotNull T[] array) {
        return toElementJsonArray(Arrays.asList(array));
    }

    @NotNull
    public static String getJsonFromGET(@NotNull HttpRequest request, @Nullable HttpEntity entity, long timeoutSeconds) throws Exception {

        Objects.requireNonNull(request);

        CloseableCollector collector = new CloseableCollector();
        String jsonString;

        try {

            RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(timeoutSeconds)).setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds)).build();
            //ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(20)).build();
            CloseableHttpClient httpClient = collector.put(HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build());
            HttpGet get = new HttpGet(request.getUrl());
            request.getHeader().forEach(get::addHeader);
            if (entity != null) get.setEntity(collector.put(entity));
            CloseableHttpResponse response = collector.put(httpClient.execute(get));
            Scanner scanner = collector.put(new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8));

            StringBuilder content = new StringBuilder();
            while(scanner.hasNext()) {
                content.append(scanner.nextLine());
            }

            jsonString = content.toString();

        } catch (Exception ex) {
            collector.closeQuietly();
            throw ex;
        }

        collector.closeQuietly();

        return jsonString;

    }

    @NotNull
    public static String getJsonFromPOST(@NotNull HttpRequest request, @Nullable HttpEntity entity, long timeoutSeconds) throws Exception {

        Objects.requireNonNull(request);

        CloseableCollector collector = new CloseableCollector();
        String jsonString;

        try {

            RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(timeoutSeconds)).setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds)).build();
            //ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(20)).build();
            CloseableHttpClient httpClient = collector.put(HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build());
            HttpPost post = new HttpPost(request.getUrl());
            request.getHeader().forEach(post::addHeader);
            if (entity != null) post.setEntity(collector.put(entity));
            CloseableHttpResponse response = collector.put(httpClient.execute(post));
            
            // Check HTTP status code
            int statusCode = response.getCode();
            if (statusCode < 200 || statusCode >= 300) {
                Scanner errorScanner = collector.put(new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder errorContent = new StringBuilder();
                while(errorScanner.hasNext()) {
                    errorContent.append(errorScanner.nextLine());
                }
                LOGGER.error("HTTP POST request failed with status code: " + statusCode + ", response: " + errorContent.toString());
                throw new Exception("HTTP request failed with status " + statusCode + ": " + errorContent.toString());
            }
            
            Scanner scanner = collector.put(new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8));

            StringBuilder content = new StringBuilder();
            while(scanner.hasNext()) {
                content.append(scanner.nextLine());
            }

            jsonString = content.toString();

        } catch (Exception ex) {
            collector.closeQuietly();
            throw ex;
        }

        collector.closeQuietly();

        return jsonString;

    }

}
