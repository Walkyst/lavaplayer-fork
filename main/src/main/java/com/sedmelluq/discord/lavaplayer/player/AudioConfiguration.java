package com.sedmelluq.discord.lavaplayer.player;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.track.playback.AllocatingAudioFrameBuffer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory;

/**
 * Configuration for audio processing.
 */
public class AudioConfiguration {
  public static final int OPUS_QUALITY_MAX = 10;

  private volatile ResamplingQuality resamplingQuality;
  private volatile int opusEncodingQuality;
  private volatile AudioDataFormat outputFormat;
  private volatile boolean filterHotSwapEnabled;
  private volatile AudioFrameBufferFactory frameBufferFactory;

  /**
   * Create a new configuration with default values.
   */
  public AudioConfiguration() {
    resamplingQuality = ResamplingQuality.LOW;
    opusEncodingQuality = OPUS_QUALITY_MAX;
    outputFormat = StandardAudioDataFormats.DISCORD_OPUS;
    filterHotSwapEnabled = false;
    frameBufferFactory = AllocatingAudioFrameBuffer::new;
  }

  public ResamplingQuality getResamplingQuality() {
    return resamplingQuality;
  }

  public AudioConfiguration setResamplingQuality(ResamplingQuality resamplingQuality) {
    this.resamplingQuality = resamplingQuality;
    return this;
  }

  public int getOpusEncodingQuality() {
    return opusEncodingQuality;
  }

  public AudioConfiguration setOpusEncodingQuality(int opusEncodingQuality) {
    this.opusEncodingQuality = Math.max(0, Math.min(opusEncodingQuality, OPUS_QUALITY_MAX));
    return this;
  }

  public AudioDataFormat getOutputFormat() {
    return outputFormat;
  }

  public AudioConfiguration setOutputFormat(AudioDataFormat outputFormat) {
    this.outputFormat = outputFormat;
    return this;
  }

  public boolean isFilterHotSwapEnabled() {
    return filterHotSwapEnabled;
  }

  public AudioConfiguration setFilterHotSwapEnabled(boolean filterHotSwapEnabled) {
    this.filterHotSwapEnabled = filterHotSwapEnabled;
    return this;
  }

  public AudioFrameBufferFactory getFrameBufferFactory() {
    return frameBufferFactory;
  }

  public AudioConfiguration setFrameBufferFactory(AudioFrameBufferFactory frameBufferFactory) {
    this.frameBufferFactory = frameBufferFactory;
    return this;
  }

  /**
   * @return A copy of this configuration.
   */
  public AudioConfiguration copy() {
    return new AudioConfiguration()
            .setResamplingQuality(resamplingQuality)
            .setOpusEncodingQuality(opusEncodingQuality)
            .setOutputFormat(outputFormat)
            .setFilterHotSwapEnabled(filterHotSwapEnabled)
            .setFrameBufferFactory(frameBufferFactory);
  }

  /**
   * Resampling quality levels
   */
  public enum ResamplingQuality {
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW
  }
}
