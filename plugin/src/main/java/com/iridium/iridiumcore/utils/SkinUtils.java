package com.iridium.iridiumcore.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iridium.iridiumcore.IridiumCore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class SkinUtils {

    private static final HashMap<UUID, String> cache = new HashMap<>();
    private static final HashMap<String, UUID> uuidCache = new HashMap<>();
    private static final Gson gson = new Gson();

    private static final String steveSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI3YWY5ZTQ0MTEyMTdjN2RlOWM2MGFjYmQzYzNmZDY1MTk3ODMzMzJhMWIzYmM1NmZiZmNlOTA3MjFlZjM1In19fQ==";

    public static UUID getUUID(String username) {
        if (!uuidCache.containsKey(username.toLowerCase())) {
            try {
                String signature = getURLContent("https://api.mojang.com/users/profiles/minecraft/" + username);
                if (signature.isEmpty()) {
                    uuidCache.put(username.toLowerCase(), UUID.randomUUID());
                } else {
                    JsonObject profileJsonObject = gson.fromJson(signature, JsonObject.class);
                    String value = profileJsonObject.get("id").getAsString();
                    if (value == null) return UUID.randomUUID();
                    uuidCache.put(username.toLowerCase(),
                            UUID.fromString(value.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5"
                            )));
                }
            } catch (UnknownHostException exception) {
                return UUID.randomUUID();
            }
        }
        return uuidCache.get(username.toLowerCase());
    }

    public static String getHeadData(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            try {
                String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
                if (signature.isEmpty()) {
                    cache.put(uuid, steveSkin);
                } else {

                    JsonObject profileJsonObject = gson.fromJson(signature, JsonObject.class);
                    if (!profileJsonObject.has("properties")) return steveSkin;
                    String value = profileJsonObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                    String decoded = new String(Base64.getDecoder().decode(value));

                    JsonObject textureJsonObject = gson.fromJson(decoded, JsonObject.class);
                    String skinURL = textureJsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                    byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();
                    String data = new String(Base64.getEncoder().encode(skinByte));
                    cache.put(uuid, data);
                }
            } catch (UnknownHostException exception) {
                return steveSkin;
            }
        }
        return cache.get(uuid);
    }

    private static String getURLContent(String urlStr) throws UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader in = null;
        try {
            URL url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) stringBuilder.append(str);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
}
