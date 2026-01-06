package com.avenqo.cucumber.beapp.driver;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Provides a Map<String, ?> of items read from JSON file.
 *
 */
@Slf4j
public abstract class AbstractMapFromInputStreamProvider {

    private final String rootPath;

    protected AbstractMapFromInputStreamProvider(String rootPath) {
        this.rootPath = rootPath;
    }

    protected Map<String, ?> createMapFromJson() {
        log.info("");
        var filePath = rootPath + getFileNameWithoutExtension() + ".json";

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }

        URL resourceUrl = cl.getResource(filePath);
        if (resourceUrl == null) {
            throw new RuntimeException("Aborting. File '" + filePath + "' not found on classpath!");
        }

        // 3) Stream öffnen und JSON parsen
        try (var in = cl.getResourceAsStream(filePath);
             var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            return new Gson().fromJson(reader, Map.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON resource '" + filePath + "'.", e);
        }
    }

    protected abstract String getFileNameWithoutExtension();
}
