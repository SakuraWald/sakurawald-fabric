package io.github.sakurawald.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sakurawald.ServerMain;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@UtilityClass
@Slf4j
public class MessageUtil {
    private static final FabricServerAudiences adventure = FabricServerAudiences.of(ServerMain.SERVER);
    @Getter
    private static final HashMap<String, String> player2lang = new HashMap<>();
    @Getter
    private static final HashMap<String, JsonObject> lang2json = new HashMap<>();
    private static final String DEFAULT_LANG = "en_us";
    private static final MiniMessage miniMessage = MiniMessage.builder().build();
    private static final Path LANGUAGE_PATH = ServerMain.CONFIG_PATH.resolve("language");

    static {
        copyLanguageFiles();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void copyLanguageFiles() {
        Path STORAGE_PATH = ServerMain.CONFIG_PATH.resolve("language").toAbsolutePath();
        if (!Files.exists(STORAGE_PATH)) {
            log.info("Create language folder.");
            try {
                Files.createDirectories(STORAGE_PATH);
                Files.copy(
                        FabricLoader.getInstance().getModContainer(ServerMain.MOD_ID).flatMap(modContainer -> modContainer.findPath("assets/sakurawald/lang/en_us.json")).get(),
                        STORAGE_PATH.resolve("en_us.json")
                );
                Files.copy(
                        FabricLoader.getInstance().getModContainer(ServerMain.MOD_ID).flatMap(modContainer -> modContainer.findPath("assets/sakurawald/lang/zh_cn.json")).get(),
                        STORAGE_PATH.resolve("zh_cn.json")
                );

            } catch (IOException e) {
                log.warn("Failed to create language folder -> {}", e.getMessage());
            }
        }
    }

    public static void loadLanguageIfAbsent(String lang) {
        if (lang2json.containsKey(lang)) return;

        InputStream is;
        try {
            is = FileUtils.openInputStream(LANGUAGE_PATH.resolve(lang + ".json").toFile());
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            lang2json.put(lang, jsonObject);
            log.info("Language {} loaded.", lang);
        } catch (IOException e) {
            log.debug("One of your player is using a language '{}' that is missing -> fallback to default language for this player", lang);
        }

        if (!lang2json.containsKey(DEFAULT_LANG)) loadLanguageIfAbsent(DEFAULT_LANG);
    }


    public static String ofString(Audience audience, String key, Object... args) {

        /* get player */
        ServerPlayer player;
        if (audience instanceof ServerPlayer) player = (ServerPlayer) audience;
        else if (audience instanceof CommandSourceStack source && source.getPlayer() != null)
            player = source.getPlayer();
        else player = null;

        /* get lang */
        String lang;
        if (player != null) {
            lang = player2lang.getOrDefault(player.getGameProfile().getName(), DEFAULT_LANG);
        } else {
            lang = DEFAULT_LANG;
        }

        loadLanguageIfAbsent(lang);

        /* get json */
        JsonObject json;
        json = lang2json.get(!lang2json.containsKey(lang) ? DEFAULT_LANG : lang);
        if (!json.has(key)) {
            log.warn("Language {} miss key '{}' -> fallback to default language for this key", lang, key);
            json = lang2json.get(DEFAULT_LANG);
        }

        /* get value */
        String value;
        value = json.get(key).getAsString();
        if (args.length > 0) {
            value = String.format(value, args);
        }

        return value;
    }

    public static Component ofComponent(Audience audience, String key, Object... args) {
        return ofComponent(ofString(audience, key, args));
    }

    public static Component ofComponent(String str) {
        return miniMessage.deserialize(str);
    }

    // todo: auto add keys in language
    public static net.minecraft.network.chat.Component ofVomponent(String str) {
        return toVomponent(ofComponent(str));
    }

    public static net.minecraft.network.chat.Component ofVomponent(Audience audience, String key, Object... args) {
        return adventure.toNative(ofComponent(audience, key, args));
    }

    public static net.minecraft.network.chat.Component toVomponent(Component component) {
        return adventure.toNative(component);
    }

    public static List<net.minecraft.network.chat.Component> ofVomponents(Audience audience, String key, Object... args) {
        String lines = ofString(audience, key, args);
        List<net.minecraft.network.chat.Component> ret = new ArrayList<>();
        for (String line : lines.split("\n")) {
            ret.add(adventure.toNative(miniMessage.deserialize(line)));
        }
        return ret;
    }

    public static void sendMessage(Audience audience, String key, Object... args) {
        audience.sendMessage(ofComponent(audience, key, args));
    }

    public static void sendActionBar(Audience audience, String key, Object... args) {
        audience.sendActionBar(ofComponent(audience, key, args));
    }

    public static void sendBroadcast(String key, Object... args) {
        // fix: log broadcast for console
        log.info(PlainTextComponentSerializer.plainText().serialize(ofComponent(null, key, args)));

        for (ServerPlayer player : ServerMain.SERVER.getPlayerList().getPlayers()) {
            sendMessage(player, key, args);
        }
    }

}
