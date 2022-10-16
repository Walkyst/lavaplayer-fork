package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.grack.nanojson.JsonWriter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class YoutubeClientConfig {
    public static final String ANDROID_CLIENT_VERSION = "17.36.4"; // 17.39.35
    public static final AndroidVersion DEFAULT_ANDROID_VERSION = AndroidVersion.ANDROID_11;

    // Clients
    public static YoutubeClientConfig ANDROID = new YoutubeClientConfig()
            // yes this is a weird way of doing it but the logic behind it is that this config can be overwritten by users
            // if needed. This allows users to also override the user agent string which is used by the YoutubeHttpContextFilter.
            // Android %s; US)
            .withUserAgent(String.format("com.google.android.youtube/%s (Linux; U; Android %s) gzip", ANDROID_CLIENT_VERSION, DEFAULT_ANDROID_VERSION.getOsVersion()))
            .withClientName("ANDROID")
            .withClientField("clientVersion", ANDROID_CLIENT_VERSION)
            .withClientField("androidSdkVersion", DEFAULT_ANDROID_VERSION.getSdkVersion())
            .withClientField("osName", "Android")
            .withClientField("osVersion", DEFAULT_ANDROID_VERSION.getOsVersion());
//            .withClientField("platform", "MOBILE")
//            .withClientField("hl", "en-US")
//            .withClientField("gl", "US")
//            .withUserField("lockedSafetyMode", false);

    public static YoutubeClientConfig IOS = new YoutubeClientConfig()
            .withUserAgent("com.google.ios.youtube/17.36.4 (iPhone14,5; U; CPU iOS 15_6 like Mac OS X)")
            .withClientName("IOS")
            .withClientField("clientVersion", "17.36.4")
            .withClientField("deviceMake", "Apple")
            .withClientField("deviceModel", "iPhone14,5")
            .withClientField("platform", "MOBILE")
            .withClientField("osName", "iOS")
            .withClientField("osVersion", "15.6.0.19G71");
//            .withClientField("hl", "en-US")
//            .withClientField("gl", "US")
//            .withUserField("lockedSafetyMode", false)

    public static YoutubeClientConfig TV_EMBEDDED = new YoutubeClientConfig()
            .withClientName("TVHTML5_SIMPLY_EMBEDDED_PLAYER")
            .withClientField("clientVersion", "2.0");
            // platform TV

    public static YoutubeClientConfig WEB = new YoutubeClientConfig()
            .withClientName("WEB")
            .withClientField("clientVersion", "2.20220801.00.00");
            // platform DESKTOP

    public static YoutubeClientConfig MUSIC = new YoutubeClientConfig()
            .withClientName("WEB_REMIX")
            .withClientField("clientVersion", "1.20220727.01.00");

    // root.cpn => content playback nonce, a-zA-Z0-9-_ (16 characters)
    // contextPlaybackContext.refer => url (video watch URL?)

    private String name;

    private String userAgent;

    private final Map<String, Object> root;

    public YoutubeClientConfig() {
        this.root = new HashMap<>();
        this.userAgent = null;
        this.name = null;
    }

    private YoutubeClientConfig(Map<String, Object> context, String userAgent, String name) {
        this.root = context;
        this.userAgent = userAgent;
        this.name = name;
    }

    public YoutubeClientConfig copy() {
        return new YoutubeClientConfig(new HashMap<>(this.root), this.userAgent, this.name);
    }

    public YoutubeClientConfig withClientName(String name) {
        this.name = name;
        withClientField("clientName", name);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public YoutubeClientConfig withUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getUserAgent() {
        return this.userAgent;
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

    public YoutubeClientConfig withUserField(String key, Object value) {
        Map<String, Object> context = (Map<String, Object>) root.computeIfAbsent("context", __ -> new HashMap<String, Object>());
        Map<String, Object> user = (Map<String, Object>) context.computeIfAbsent("user", __ -> new HashMap<String, Object>());
        user.put(key, value);
        return this;
    }

    public String toJsonString() {
        return JsonWriter.string().object(root).done();
    }

    public enum AndroidVersion {
        // https://apilevels.com/
        ANDROID_13("13", 33),
        ANDROID_12("12", 31), // 12L => 32
        ANDROID_11("11", 30);

        private final String osVersion;
        private final int sdkVersion;

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
