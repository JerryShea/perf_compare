package com.bigpaws.agrona;

import com.bigpaws.AbstractInvoke;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public class AgronaTest extends AbstractInvoke {
    private static final long SIZE = (2 << 20) + RingBufferDescriptor.TRAILER_LENGTH;
    private static final String PATH = IoUtil.tmpDirName() + "/eg-ring-buffer";
    private final FileChannel channel;
    private final OneToOneRingBuffer ringBuffer;
    private final ExpandableArrayBuffer src;

    AgronaTest(LongConsumer consumer, String payload) throws IOException {
        super(consumer, payload);
        IoUtil.delete(new File(PATH), true);
        final RandomAccessFile file = new RandomAccessFile(PATH, "rw");
        file.setLength(SIZE);
        this.channel = file.getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, SIZE);
        UnsafeBuffer ub = new UnsafeBuffer(buffer);
        this.ringBuffer = new OneToOneRingBuffer(ub);
        this.src = new ExpandableArrayBuffer();
        this.consumerThread.start();
    }

    @Override
    public void test(long startTimeNS) {
        src.putLong(0, startTimeNS);
        src.putStringWithoutLengthAscii(LONG_LENGTH, payload);
        boolean ok = ringBuffer.write(99, src, 0, payload.length() + LONG_LENGTH);
        if (! ok)
            throw new IllegalStateException("Could not send " + this.toString());
    }

    @Override
    protected void read(LongConsumer consumer) {
        int read = ringBuffer.read((msgTypeId, buffer, index, length) -> {
            long sentTime = buffer.getLong(index);
            consumer.accept(sentTime);
        });
    }

    @Override
    public void close() throws Exception {
        super.close();
        channel.close();
    }

    @Override
    public String toString() {
        return "AgronaTest{" +
                '}';
    }
}
