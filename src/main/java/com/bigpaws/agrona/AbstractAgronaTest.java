package com.bigpaws.agrona;

import com.bigpaws.AbstractInvoke;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.ringbuffer.RingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public abstract class AbstractAgronaTest extends AbstractInvoke {
    protected static final long SIZE = (32 << 20) + RingBufferDescriptor.TRAILER_LENGTH;
    protected static final String PATH = IoUtil.tmpDirName() + "/eg-ring-buffer";
    protected final List<Closeable> closeables = new ArrayList<>();
    private final RingBuffer ringBuffer;
    private final ExpandableArrayBuffer src;
    private final MessageHandler messageHandler;

    AbstractAgronaTest(LongConsumer consumer, String payload) throws IOException {
        super(consumer, payload);
        ringBuffer = createRingBuffer();
        this.src = new ExpandableArrayBuffer();
        this.messageHandler = (msgTypeId, buffer, index, length) -> {
            long sentTime = buffer.getLong(index);
            consumer.accept(sentTime);
        };
        this.consumerThread.start();
    }

    protected abstract RingBuffer createRingBuffer() throws IOException;

    @Override
    public void test(long startTimeNS) {
        src.putLong(0, startTimeNS);
        src.putStringWithoutLengthAscii(LONG_LENGTH, payload);
        boolean ok = ringBuffer.write(99, src, 0, payload.length() + LONG_LENGTH);
        if (! ok)
            throw new IllegalStateException("Could not send " + this.toString());
    }

    @Override
    protected void read() {
        /*int read =*/ ringBuffer.read(messageHandler);
    }

    @Override
    public void close() throws Exception {
        super.close();
        net.openhft.chronicle.core.io.Closeable.closeQuietly(closeables);
    }

    @Override
    public String toString() {
        return "AgronaTest{" +
                '}';
    }
}
