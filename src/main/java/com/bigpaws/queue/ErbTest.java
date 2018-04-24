package com.bigpaws.queue;

import com.bigpaws.AbstractInvoke;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesRingBuffer;
import net.openhft.chronicle.bytes.NativeBytes;
import net.openhft.chronicle.bytes.NativeBytesStore;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public class ErbTest extends AbstractInvoke {
    private final BytesRingBuffer ringBuffer;
    private final Bytes read;
    private final Bytes write;
    private final NativeBytesStore erbBytes;

    ErbTest(LongConsumer consumer, String payload) {
        super(consumer, payload);
        read = Bytes.elasticByteBuffer();
        write = NativeBytes.nativeBytes(128);
        erbBytes = NativeBytesStore.nativeStoreWithFixedCapacity(BytesRingBuffer.sizeFor(32 << 20));
        ringBuffer = BytesRingBuffer.newInstance(erbBytes);
        consumerThread.start();
    }

    @Override
    public void test(long startTimeNS) {
        write.writePosition(0);
        write.writeLong(startTimeNS).write8bit(payload);
        write.readLimit(write.writePosition());
        write.readPosition(0);
        boolean ok = ringBuffer.offer(write);
        if (! ok)
            throw new IllegalStateException("Could not send " + this.toString());
    }

    @Override
    protected void read() {
        read.clear();
        if (ringBuffer.read(read)) {
            long sentTime = read.readLong();
            consumer.accept(sentTime);
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        read.release();
        write.release();
        erbBytes.release();
    }
}
