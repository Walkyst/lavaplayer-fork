package com.sedmelluq.discord.lavaplayer.integration;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeClientConfig;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetails;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackFormat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FormatCipherTest {

    public static void main(String[] args) {
        AudioPlayerManager apm = new DefaultAudioPlayerManager();
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager();
        apm.registerSourceManager(yt);

        YoutubeClientConfig config = new YoutubeClientConfig()
                .withClientName("WEB")
                .withClientField("clientVersion", "2.20220918");

        YoutubeTrackDetails details = yt.getTrackDetailsLoader().loadDetails(yt.getHttpInterface(), "dNUHmLHkKXI", true, yt, config);
        List<YoutubeTrackFormat> formats = details.getFormats(yt.getHttpInterface(), yt.getSignatureResolver());

        formats.stream()
                .filter(format -> format.getInfo() != null)
                .filter(format -> "audio/webm".equals(format.getInfo().mimeType))
                .sorted(Comparator.comparingLong(YoutubeTrackFormat::getBitrate).reversed())
                .forEach(format -> System.out.printf("opus %dk %dc ciphered? %b%n", (format.getBitrate() / 1024), format.getAudioChannels(), format.getSignature() != null));
    }

}
