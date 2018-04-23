package com.bigpaws.agrona;

import org.agrona.IoUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 23/04/18.
 */
public class AgronaOneToOneFileTest extends AbstractAgronaTest {
    public AgronaOneToOneFileTest(LongConsumer longConsumer, String s) throws IOException {
        super(longConsumer, s);
    }

    @Override
    protected RingBuffer createRingBuffer() throws IOException {
        IoUtil.delete(new File(PATH), true);
        final RandomAccessFile file = new RandomAccessFile(PATH, "rw");
        file.setLength(SIZE);
        final FileChannel channel = file.getChannel();
        closeables.add(channel);
        MappedByteBuffer mbbuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, SIZE);
        UnsafeBuffer ub = new UnsafeBuffer(mbbuffer);
        return new OneToOneRingBuffer(ub);
    }
}
