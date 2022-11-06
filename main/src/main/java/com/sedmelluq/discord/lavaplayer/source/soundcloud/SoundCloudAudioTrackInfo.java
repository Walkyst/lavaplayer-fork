package com.sedmelluq.discord.lavaplayer.source.soundcloud;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

/**
 * Meta info for an audio track
 */
public class SoundCloudAudioTrackInfo extends AudioTrackInfo {
    /**
     * Track title
     */
    public final String title;
    /**
     * Track author, if known
     */
    public final String author;
    /**
     * Length of the track in milliseconds, UnitConstants.DURATION_MS_UNKNOWN for streams
     */
    public final long length;
    /**
     * Audio source specific track identifier
     */
    public final String identifier;
    /**
     * True if this track is a stream
     */
    public final boolean isStream;
    /**
     * URL of the track, or local path to the file.
     */
    public final String uri;
    /*
    * Whether the track is a SoundCloud GO+ Demo
    * */
    public final boolean isDemo;

    /**
     * @param title Track title
     * @param author Track author, if known
     * @param length Length of the track in milliseconds
     * @param identifier Audio source specific track identifier
     * @param isStream True if this track is a stream
     * @param uri URL of the track or path to its file.
     */

    public SoundCloudAudioTrackInfo(String title, String author, long length, String identifier, boolean isStream, String uri, Boolean isDemo) {
        super(title, author, length, identifier, isStream, uri);
        this.title = title;
        this.author = author;
        this.length = length;
        this.identifier = identifier;
        this.isStream = isStream;
        this.uri = uri;
        this.isDemo = isDemo;
    }
}
