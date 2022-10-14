package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.sedmelluq.discord.lavaplayer.container.matroska.MatroskaAudioTrack;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sedmelluq.discord.lavaplayer.container.Formats.MIME_AUDIO_WEBM;
import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.decodeUrlEncodedItems;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.Units.CONTENT_LENGTH_UNKNOWN;

/**
 * Audio track that handles processing Youtube videos as audio tracks.
 */
public class YoutubeAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(YoutubeAudioTrack.class);

  private static final int MAX_RETRIES = 3;

  private final YoutubeAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param sourceManager Source manager which was used to find this track
   */
  public YoutubeAudioTrack(AudioTrackInfo trackInfo, YoutubeAudioSourceManager sourceManager) {
    super(trackInfo);

    this.sourceManager = sourceManager;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    for (int i = MAX_RETRIES; i > 0; i--) {
      // Try load formats. This seems janky, but we retry in hopes of obtaining a URL with a different cipher --
      // one that can be deciphered and played. This is more of a workaround than a legit fix.
      try {
        processInternal(localExecutor);
      } catch (ForbiddenException e) {
        // note for later: Experiment with setting android client user-agent header.
        // with the recent changes to details loading, gv URLs may now expect the header to be present.
        if (i > 1) {
          log.warn("Received 403 response when attempting to load track. Retrying (attempt {}/{})", (MAX_RETRIES - i) + 1, MAX_RETRIES);
          continue;
        }

        FormatWithUrl format = e.format;
        log.warn("Failed to play {}\n\tCiphered URL: {}\n\tDeciphered URL: {}\n\tSignature Key: {}\n\tSignature: {}\n\tPlayer Script URL: {}\n\tFormat String:{}",
                trackInfo.identifier, format.details.getUrl().toString(), format.signedUrl, format.details.getSignatureKey(), format.details.getSignature(),
                format.playerScriptUrl, format.details.getExtra());
        throw (Exception) e.getCause();
      }
    }
  }

  private void processInternal(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      FormatWithUrl format = loadBestFormatWithUrl(httpInterface);

      log.debug("Starting track from URL: {}", format.signedUrl);

      if (trackInfo.isStream || format.details.getContentLength() == CONTENT_LENGTH_UNKNOWN) {
        processStream(localExecutor, format);
      } else {
        processStatic(localExecutor, httpInterface, format, false);
      }
    }
  }

  private void processStatic(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface, FormatWithUrl format, boolean isFallback) throws Exception {
    httpInterface.getContext().setAttribute(YoutubeHttpContextFilter.ATTRIBUTE_ANDROID_REQUEST, true);
    try (YoutubePersistentHttpStream stream = new YoutubePersistentHttpStream(httpInterface, format.signedUrl, format.details.getContentLength())) {
      if (format.details.getType().getMimeType().endsWith("/webm")) {
        processDelegate(new MatroskaAudioTrack(trackInfo, stream), localExecutor);
      } else {
        processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
      }
    } catch (RuntimeException e) {
      if (!isFallback) {
        FormatWithUrl fallback = format.getFallback();

        if (fallback != null) {
          log.warn("Falling back with {}", fallback.signedUrl);
          processStatic(localExecutor, httpInterface, fallback, true);
          return;
        }
      }

      if (e.getMessage().equals("Not success status code: 403")) {
        throw new ForbiddenException(format, e);
      }

      throw e;
    }
  }

  private void processStream(LocalAudioTrackExecutor localExecutor, FormatWithUrl format) throws Exception {
    if (MIME_AUDIO_WEBM.equals(format.details.getType().getMimeType())) {
      throw new FriendlyException("YouTube WebM streams are currently not supported.", COMMON, null);
    } else {
      try (HttpInterface streamingInterface = sourceManager.getHttpInterface()) {
        processDelegate(new YoutubeMpegStreamAudioTrack(trackInfo, streamingInterface, format.signedUrl), localExecutor);
      }
    }
  }

  private FormatWithUrl loadBestFormatWithUrl(HttpInterface httpInterface) throws Exception {
    YoutubeTrackDetails details = sourceManager.getTrackDetailsLoader()
        .loadDetails(httpInterface, getIdentifier(), true, sourceManager);

    // If the error reason is "Video unavailable" details will return null
    if (details == null) {
      throw new FriendlyException("This video is not available", FriendlyException.Severity.COMMON, null);
    }

    List<YoutubeTrackFormat> formats = details.getFormats(httpInterface, sourceManager.getSignatureResolver());

    YoutubeTrackFormat format = findBestSupportedFormat(formats);

    URI signedUrl = sourceManager.getSignatureResolver()
        .resolveFormatUrl(httpInterface, details.getPlayerScript(), format);

    return new FormatWithUrl(format, signedUrl, details.getPlayerScript());
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new YoutubeAudioTrack(trackInfo, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }

  private static boolean isBetterFormat(YoutubeTrackFormat format, YoutubeTrackFormat other) {
    YoutubeFormatInfo info = format.getInfo();

    if (info == null) {
      return false;
    } else if (other == null) {
      return true;
    } else if (info.mimeType.equals("audio/webm") && format.getAudioChannels() > 2) {
      // Opus with more than 2 audio channels is unsupported by LavaPlayer currently.
      return false;
    } else if (info.ordinal() != other.getInfo().ordinal()) {
      return info.ordinal() < other.getInfo().ordinal();
    } else {
      return format.getBitrate() > other.getBitrate();
    }
  }

  private static YoutubeTrackFormat findBestSupportedFormat(List<YoutubeTrackFormat> formats) {
    YoutubeTrackFormat bestFormat = null;

    for (YoutubeTrackFormat format : formats) {
      if (isBetterFormat(format, bestFormat)) {
        bestFormat = format;
      }
    }

    if (bestFormat == null) {
      StringJoiner joiner = new StringJoiner(", ");
      formats.forEach(format -> joiner.add(format.getType().toString()));
      throw new IllegalStateException("No supported audio streams available, available types: " + joiner);
    }

    return bestFormat;
  }

  private static class FormatWithUrl {
    private final YoutubeTrackFormat details;
    private final URI signedUrl;
    private final String playerScriptUrl;

    private FormatWithUrl(YoutubeTrackFormat details, URI signedUrl, String playerScriptUrl) {
      this.details = details;
      this.signedUrl = signedUrl;
      this.playerScriptUrl = playerScriptUrl;
    }

    public FormatWithUrl getFallback() {
      String signedUrl = this.signedUrl.toString();
      Map<String, String> urlParameters = decodeUrlEncodedItems(signedUrl, false);

      String mn = urlParameters.get("mn");

      if (mn == null) {
        return null;
      }

      String[] hosts = mn.split(",");

      if (hosts.length < 2) {
        log.warn("Cannot fallback, available hosts: {}", String.join(", ", hosts));
        return null;
      }

      String newUrl = signedUrl.replace(hosts[0], hosts[1]);

      try {
        URI uri = new URI(newUrl);
        return new FormatWithUrl(details, uri, playerScriptUrl);
      } catch (URISyntaxException e) {
        return null;
      }
    }
  }

  private static class ForbiddenException extends Exception {
    private final FormatWithUrl format;

    public ForbiddenException(FormatWithUrl format, Throwable original) {
      super(null, original, false, false);
      this.format = format;
    }
  }
}
