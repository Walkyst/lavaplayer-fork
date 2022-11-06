package com.sedmelluq.discord.lavaplayer.source.soundcloud;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSoundCloudDataReader implements SoundCloudDataReader {
  private static final Logger log = LoggerFactory.getLogger(DefaultSoundCloudDataReader.class);

  @Override
  public JsonBrowser findTrackData(JsonBrowser rootData) {
    return findEntryOfKind(rootData, "track");
  }

  @Override
  public String readTrackId(JsonBrowser trackData) {
    return trackData.get("id").safeText();
  }

  @Override
  public boolean isTrackBlocked(JsonBrowser trackData) {
    return "BLOCK".equals(trackData.get("policy").safeText()) || "SUB_HIGH_TIER".equals(trackData.get("monetization_model").safeText());
  }

  @Override
  public SoundCloudAudioTrackInfo readTrackInfo(JsonBrowser trackData, String identifier) {
    Integer duration = trackData.get("full_duration").as(Integer.class);
    boolean isDemo = false;
    if ("SUB_HIGH_TIER".equals(trackData.get("monetization_model").safeText())) {
      duration = 30000;
      isDemo = true;
    }

    return new SoundCloudAudioTrackInfo(
            trackData.get("title").safeText(),
            trackData.get("user").get("username").safeText(),
            duration,
            identifier,
            false,
            trackData.get("permalink_url").text(),
            isDemo
    );
  }

  @Override
  public List<SoundCloudTrackFormat> readTrackFormats(JsonBrowser trackData) {
    List<SoundCloudTrackFormat> formats = new ArrayList<>();
    String trackId = readTrackId(trackData);

    if (trackId.isEmpty()) {
      log.warn("Track data {} missing track ID: {}.", trackId, trackData.format());
    }

    for (JsonBrowser transcoding : trackData.get("media").get("transcodings").values()) {
      JsonBrowser format = transcoding.get("format");

      String protocol = format.get("protocol").safeText();
      String mimeType = format.get("mime_type").safeText();

      if (!protocol.isEmpty() && !mimeType.isEmpty()) {
        String lookupUrl = transcoding.get("url").safeText();

        if (!lookupUrl.isEmpty()) {
          formats.add(new DefaultSoundCloudTrackFormat(trackId, protocol, mimeType, lookupUrl));
        } else {
          log.warn("Transcoding of {} missing url: {}.", trackId, transcoding.format());
        }
      } else {
        log.warn("Transcoding of {} missing protocol/mimetype: {}.", trackId, transcoding.format());
      }
    }

    return formats;
  }

  @Override
  public JsonBrowser findPlaylistData(JsonBrowser rootData, String kind) {
    return findEntryOfKind(rootData, kind);
  }

  @Override
  public String readPlaylistName(JsonBrowser playlistData) {
    return playlistData.get("title").safeText();
  }

  @Override
  public String readPlaylistIdentifier(JsonBrowser playlistData) {
    return playlistData.get("permalink").safeText();
  }

  @Override
  public List<JsonBrowser> readPlaylistTracks(JsonBrowser playlistData) {
    return playlistData.get("tracks").values();
  }

  protected JsonBrowser findEntryOfKind(JsonBrowser data, String kind) {
    if (data.isMap() && kind.equals(data.get("kind").text())) {
      return data;
    }

    return null;
  }
}
