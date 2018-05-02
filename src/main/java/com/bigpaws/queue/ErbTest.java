package com.bigpaws.queue;

import com.bigpaws.AbstractInvoke;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesRingBuffer;
import net.openhft.chronicle.bytes.NativeBytes;
import net.openhft.chronicle.bytes.NativeBytesStore;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public class ErbTest extends AbstractInvoke {
    private final BytesRingBuffer ringBuffer;
    private final Bytes read;
    private final Bytes write;
    private final NativeBytesStore erbBytes;

    ErbTest(TwoLongConsumer consumer, String payload) {
        super(consumer, payload);
        read = Bytes.elasticByteBuffer();
        write = NativeBytes.nativeBytes(128);
        write.writeLong(Long.MIN_VALUE).write(payload.getBytes());
        erbBytes = NativeBytesStore.nativeStoreWithFixedCapacity(BytesRingBuffer.sizeFor(AbstractInvoke.BASE_BUFFER_SIZE));
        ringBuffer = BytesRingBuffer.newInstance(erbBytes);
        consumerThread.start();
    }

    @Override
    public void test(long startTimeNS) {
        write.writeLong(0, startTimeNS);
        write.readLimit(write.writePosition());
        write.readPosition(0);
        boolean ok = ringBuffer.offer(write);
        if (! ok)
            throw new IllegalStateException("Could not send " + this.toString());
    }

    @Override
    protected void read(long startReadTime) {
        read.clear();
        if (ringBuffer.read(read)) {
            long sentTime = read.readLong();
            consumer.accept(startReadTime, sentTime);
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
