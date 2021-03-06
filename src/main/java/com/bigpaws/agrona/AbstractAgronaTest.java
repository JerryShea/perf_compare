package com.bigpaws.agrona;

import com.bigpaws.AbstractInvoke;
import com.bigpaws.Invoke;
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

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public abstract class AbstractAgronaTest extends AbstractInvoke {
    protected static final long SIZE = AbstractInvoke.BASE_BUFFER_SIZE + RingBufferDescriptor.TRAILER_LENGTH;
    protected static final String PATH = IoUtil.tmpDirName() + "/eg-ring-buffer";
    protected final List<Closeable> closeables = new ArrayList<>();
    private final RingBuffer ringBuffer;
    private final ExpandableArrayBuffer src;
    private final MessageHandler messageHandler;
    private long startReadTime;

    AbstractAgronaTest(TwoLongConsumer consumer, String payload) throws IOException {
        super(consumer, payload);
        ringBuffer = createRingBuffer();
        this.src = new ExpandableArrayBuffer();
        src.putLong(0, Long.MIN_VALUE);
        src.putBytes(Invoke.LONG_LENGTH, payload.getBytes());
        this.messageHandler = (msgTypeId, buffer, index, length) -> {
            long sentTime = buffer.getLong(index);
            consumer.accept(startReadTime, sentTime);
        };
        this.consumerThread.start();
    }

    protected abstract RingBuffer createRingBuffer() throws IOException;

    @Override
    public void test(long startTimeNS) {
        src.putLong(0, startTimeNS);
        boolean ok = ringBuffer.write(99, src, 0, payload.length() + LONG_LENGTH);
        if (! ok)
            throw new IllegalStateException("Could not send " + this.toString());
    }

    @Override
    protected void read(long startReadTime) {
        this.startReadTime = startReadTime;
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
