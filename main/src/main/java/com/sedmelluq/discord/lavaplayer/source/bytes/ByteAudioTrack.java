package com.sedmelluq.discord.lavaplayer.source.bytes;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.util.Base64;

/**
 * Audio track that handles processing bytes as audio tracks.
 */
public class ByteAudioTrack extends DelegatedAudioTrack {
  private final MediaContainerDescriptor containerTrackFactory;
  private final BytesAudioSourceManager sourceManager;

  private final byte[] bytes;

  /**
   * @param trackInfo Track info
   * @param containerTrackFactory Probe track factory - contains the probe with its parameters.
   * @param sourceManager Source manager used to load this track
   */
  public ByteAudioTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory,
                        BytesAudioSourceManager sourceManager) {

    super(trackInfo);
    this.bytes = Base64.getDecoder().decode(trackInfo.identifier);
    this.containerTrackFactory = containerTrackFactory;
    this.sourceManager = sourceManager;
  }

  /**
   * @return The media probe which handles creating a container-specific delegated track for this track.
   */
  public MediaContainerDescriptor getContainerTrackFactory() {
    return containerTrackFactory;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (MemorySeekableInputStream inputStream = new MemorySeekableInputStream(bytes)) {
      processDelegate((InternalAudioTrack) containerTrackFactory.createTrack(trackInfo, inputStream), localExecutor);
    }
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new ByteAudioTrack(trackInfo, containerTrackFactory, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }
}