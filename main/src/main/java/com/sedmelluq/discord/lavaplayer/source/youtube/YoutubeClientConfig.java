package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.grack.nanojson.JsonWriter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class YoutubeClientConfig {
    public static final String ANDROID_CLIENT_VERSION = "17.39.35";
    public static final AndroidVersion DEFAULT_ANDROID_VERSION = AndroidVersion.ANDROID_11;

    // Clients
    public static YoutubeClientConfig ANDROID_CLIENT = new YoutubeClientConfig()
            .withClientField("clientName", "ANDROID")
            .withClientField("clientVersion", ANDROID_CLIENT_VERSION)
            .withClientField("androidSdkVersion", DEFAULT_ANDROID_VERSION.getSdkVersion())
            .withClientField("osName", "Android")
            .withClientField("osVersion", DEFAULT_ANDROID_VERSION.osVersion);
            //.withClientField("platform", "MOBILE")
            //.withClientField("hl", "en-GB")
            //.withClientField("gl", "US")
            //.withClientUserAgent("com.google.android.youtube/17.39.35 (Linux; U; Android 11) gzip"); // perhaps this should be set in the headers?

    public static YoutubeClientConfig TV_EMBEDDED = new YoutubeClientConfig()
            .withClientField("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
            .withClientField("clientVersion", "2.0");

    public static YoutubeClientConfig WEB = new YoutubeClientConfig()
            .withClientField("clientName", "WEB")
            .withClientField("clientVersion", "2.20220801.00.00");

    public static YoutubeClientConfig MUSIC = new YoutubeClientConfig()
            .withClientField("clientName", "WEB_REMIX")
            .withClientField("clientVersion", "1.20220727.01.00");

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

    public YoutubeClientConfig withClientDefaultScreenParameters() {
        withClientField("screenDensityFloat", 1);
        withClientField("screenHeightPoints", 1080);
        withClientField("screenPixelDensity", 1);
        return withClientField("screenWidthPoints", 1920);
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

    public YoutubeClientConfig withRootField(String key, Object value) {
        root.put(key, value);
        return this;
    }

    public YoutubeClientConfig withClientField(String key, Object value) {
        Map<String, Object> context = (Map<String, Object>) root.computeIfAbsent("context", __ -> new HashMap<String, Object>());
        Map<String, Object> client = (Map<String, Object>) context.computeIfAbsent("client", __ -> new HashMap<String, Object>());
        client.put(key, value);
        return this;
    }

    public enum AndroidVersion {
        // https://apilevels.com/
        ANDROID_13("13", 33),
        ANDROID_12("12", 31), // 12L => 32
        ANDROID_11("11", 30);

        private String osVersion;
        private int sdkVersion;

        AndroidVersion(String osVersion, int sdkVersion) {
            this.osVersion = osVersion;
            this.sdkVersion = sdkVersion;
        }

        public String getOsVersion() {
            return this.osVersion;
        }

        public int getSdkVersion() {
            return this.sdkVersion;
        }
    }
}
