package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.grack.nanojson.JsonWriter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class YoutubeClientConfig {
    // Clients
    public static YoutubeClientConfig ANDROID_CLIENT = new YoutubeClientConfig()
            .withClientName("ANDROID")
            .withClientVersion("17.39.35") // .31.35
            .withClientAndroidSdkVersion(30)
            .withClientOsName("Android")
            .withClientOsVersion("11")
            .withClientUserAgent("com.google.android.youtube/17.39.35 (Linux; U; Android 11) gzip"); // perhaps this should be set in the headers?

    public static YoutubeClientConfig TV_EMBEDDED = new YoutubeClientConfig()
            .withClientName("TVHTML5_SIMPLY_EMBEDDED_PLAYER")
            .withClientVersion("2.0");

    public static YoutubeClientConfig WEB = new YoutubeClientConfig()
            .withClientName("WEB")
            .withClientVersion("2.20220801.00.00");

    public static YoutubeClientConfig MUSIC = new YoutubeClientConfig()
            .withClientName("WEB_REMIX")
            .withClientVersion("1.20220727.01.00");

    // TODO: Maybe I should avoid creating a function per key-value and use a generic method?

    private final Map<String, Object> root;

    public YoutubeClientConfig() {
        this.root = new HashMap<>();
    }

    private YoutubeClientConfig(Map<String, Object> context) {
        this.root = context;
    }

    public YoutubeClientConfig copy() {
        return new YoutubeClientConfig(new HashMap<>(root));
    }

    public YoutubeClientConfig withRootRacyCheckOk(boolean racyCheckOk) {
        root.put("racyCheckOk", racyCheckOk);
        return this;
    }

    public YoutubeClientConfig withRootContentCheckOk(boolean contentCheckOk) {
        root.put("contentCheckOk", contentCheckOk);
        return this;
    }

    public YoutubeClientConfig withRootVideoId(String videoId) {
        root.put("videoId", videoId);
        return this;
    }

    public YoutubeClientConfig withRootPlaylistId(String playlistId) {
        root.put("playlistId", playlistId);
        return this;
    }

    public YoutubeClientConfig withRootQuery(String query) {
        root.put("query", query);
        return this;
    }

    public YoutubeClientConfig withRootParams(String params) {
        root.put("params", params);
        return this;
    }

    public YoutubeClientConfig withRootBrowseId(String browseId) {
        root.put("browseId", "VL" + browseId);
        return this;
    }

    public YoutubeClientConfig withRootContinuation(String continuation) {
        root.put("continuation", continuation);
        return this;
    }

    public YoutubeClientConfig withRoot(String key, Object value) {
        root.put(key, value);
        return this;
    }

    public YoutubeClientConfig withClientName(String clientName) {
        return putClient("clientName", clientName);
    }

    public YoutubeClientConfig withClientVersion(String clientVersion) {
        return putClient("clientVersion", clientVersion);
    }

    public YoutubeClientConfig withClientAndroidSdkVersion(int androidSdkVersion) {
        return putClient("androidSdkVersion", androidSdkVersion);
    }

    public YoutubeClientConfig withClientOsName(String osName) {
        return putClient("osName", osName);
    }

    public YoutubeClientConfig withClientOsVersion(String osVersion) {
        return putClient("osVersion", osVersion);
    }

    public YoutubeClientConfig withClientUserAgent(String osVersion) {
        return putClient("osVersion", osVersion);
    }

    public YoutubeClientConfig withClientScreen(String screen) {
        return putClient("clientScreen", screen);
    }

    public YoutubeClientConfig withClient(String key, Object value) {
        return putClient(key, value);
    }

    public YoutubeClientConfig withClientDefaultScreenParameters() {
        putClient("screenDensityFloat", 1);
        putClient("screenHeightPoints", 1080);
        putClient("screenPixelDensity", 1);
        return putClient("screenWidthPoints", 1920);
    }

    public YoutubeClientConfig withThirdPartyEmbedUrl(String embedUrl) {
        Map<String, Object> context = (Map<String, Object>) root.computeIfAbsent("context", __ -> new HashMap<String, Object>());
        Map<String, Object> thirdParty = (Map<String, Object>) context.computeIfAbsent("thirdParty", __ -> new HashMap<String, Object>());
        thirdParty.put("embedUrl", embedUrl);
        return this;
    }

    public YoutubeClientConfig withPlaybackSignatureTimestamp(String signatureTimestamp) {
        Map<String, Object> playbackContext = (Map<String, Object>) root.computeIfAbsent("playbackContext", __ -> new HashMap<String, Object>());
        Map<String, Object> contentPlaybackContext = (Map<String, Object>) playbackContext.computeIfAbsent("contentPlaybackContext", __ -> new HashMap<String, Object>());
        contentPlaybackContext.put("signatureTimestamp", signatureTimestamp);
        return this;
    }

    public String toJsonString() {
        return JsonWriter.string().object(root).done();
    }

    private YoutubeClientConfig putRoot(String key, Object value) {
        root.put(key, value);
        return this;
    }

    private YoutubeClientConfig putClient(String key, Object value) {
        Map<String, Object> context = (Map<String, Object>) root.computeIfAbsent("context", __ -> new HashMap<String, Object>());
        Map<String, Object> client = (Map<String, Object>) context.computeIfAbsent("client", __ -> new HashMap<String, Object>());
        client.put(key, value);
        return this;
    }
}
