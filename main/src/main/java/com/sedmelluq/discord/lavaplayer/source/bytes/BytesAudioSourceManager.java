package com.sedmelluq.discord.lavaplayer.source.bytes;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Base64;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Audio source manager that implements finding audio files from the local file system.
 */
public class BytesAudioSourceManager extends ProbingAudioSourceManager {
    public BytesAudioSourceManager() {
        this(MediaContainerRegistry.DEFAULT_REGISTRY);
    }

    public BytesAudioSourceManager(MediaContainerRegistry containerRegistry) {
        super(containerRegistry);
    }

    @Override
    public String getSourceName() {
        return "bytes";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        byte[] buffer = Base64.getDecoder().decode(reference.getIdentifier());
        return handleLoadResult(detectContainerForFile(reference, buffer));
    }

    @Override
    protected AudioTrack createTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory) {
        return new ByteAudioTrack(trackInfo, containerTrackFactory, this);
    }

    private MediaContainerDetectionResult detectContainerForFile(AudioReference reference, byte[] bytes) {
        try (MemorySeekableInputStream inputStream = new MemorySeekableInputStream(bytes)) {
            return new MediaContainerDetection(containerRegistry, reference, inputStream,
                    MediaContainerHints.from(null, null)).detectContainer();
        } catch (IOException e) {
            throw new FriendlyException("Failed to open bytes for reading.", SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        encodeTrackFactory(((ByteAudioTrack) track).getContainerTrackFactory(), output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        MediaContainerDescriptor containerTrackFactory = decodeTrackFactory(input);

        if (containerTrackFactory != null) {
            return new ByteAudioTrack(trackInfo, containerTrackFactory, this);
        }

        return null;
    }

    @Override
    public void shutdown() {
        // Nothing to shut down
    }
}