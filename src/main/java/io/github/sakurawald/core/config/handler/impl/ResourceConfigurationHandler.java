package io.github.sakurawald.core.config.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.abst.ConfigurationHandler;
import lombok.Cleanup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


public class ResourceConfigurationHandler extends ConfigurationHandler<JsonElement> {

    final String resourcePath;

    private ResourceConfigurationHandler(Path path, String resourcePath) {
        super(path);
        this.resourcePath = resourcePath;
    }

    public ResourceConfigurationHandler(@NotNull String resourcePath) {
        this(Fuji.CONFIG_PATH.resolve(resourcePath), resourcePath);
    }

    private static @Nullable JsonElement getJsonElement(@NotNull String resourcePath) {
        try {
            InputStream inputStream = Fuji.class.getResourceAsStream(resourcePath);
            assert inputStream != null;
            @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream));
            return JsonParser.parseReader(reader);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }

        return null;
    }

    public void loadFromDisk() {
        // Does the file exist?
        try {
            if (!path.toFile().exists()) {
                saveToDisk();
            } else {
                // read older json from disk
                @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path.toFile())));
                JsonElement olderJsonElement = JsonParser.parseReader(reader);

                // merge older json with newer json
                JsonElement newerJsonElement = getJsonElement(this.resourcePath);
                assert newerJsonElement != null;

                mergeJson(olderJsonElement, newerJsonElement);

                // read merged json
                model = olderJsonElement;
                this.saveToDisk();
            }

        } catch (Exception e) {
            LogUtil.error("load config failed", e);
        }
    }


    public void saveToDisk() {
        try {
            // Should we generate a default config instance ?
            if (!this.path.toFile().exists()) {
                LogUtil.info("write default configuration: {}", this.path.toFile().getAbsolutePath());
                Files.createDirectories(this.path.toFile().getParentFile().toPath());
                this.model = getJsonElement(this.resourcePath);
            }

            // Save.
            JsonWriter jsonWriter = gson.newJsonWriter(new BufferedWriter(new FileWriter(this.path.toFile())));
            gson.toJson(this.model, jsonWriter);
            jsonWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
