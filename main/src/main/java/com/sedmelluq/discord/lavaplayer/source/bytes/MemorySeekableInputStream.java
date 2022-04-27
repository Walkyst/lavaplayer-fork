package com.sedmelluq.discord.lavaplayer.source.bytes;

import com.sedmelluq.discord.lavaplayer.tools.io.ExtendedBufferedInputStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider;

import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Seekable input stream implementation for bytes
 */
public class MemorySeekableInputStream extends SeekableInputStream {
    private final ExtendedBufferedInputStream bufferedStream;
    private long position;

    /**
     * @param bytes Bytes to create a stream for.
     */
    public MemorySeekableInputStream(byte[] bytes) {
        super(bytes.length, 0);
        bufferedStream = new ExtendedBufferedInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public int read() throws IOException {
        int result = bufferedStream.read();
        if (result >= 0) {
            position++;
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = bufferedStream.read(b, off, len);
        position += read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = bufferedStream.skip(n);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return bufferedStream.available();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public boolean canSeekHard() {
        return true;
    }

    @Override
    public List<AudioTrackInfoProvider> getTrackInfoProviders() {
        return Collections.emptyList();
    }

    @Override
    protected void seekHard(long position) throws IOException {
        this.position = position;
        bufferedStream.discardBuffer();
    }
}
