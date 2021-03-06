package com.bigpaws.queue;

import com.bigpaws.AbstractInvoke;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.queue.BufferMode;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import org.agrona.IoUtil;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public class QueueTest extends AbstractInvoke {
    private final ExcerptTailer tailer;
    private final ExcerptAppender appender;
    private final Bytes<ByteBuffer> bytes;

    QueueTest(TwoLongConsumer consumer, String payload) {
        this(consumer, payload, ".");
    }

    QueueTest(TwoLongConsumer consumer, String payload, String basedir) {
        super(consumer, payload);
        String path = basedir + "/queue";
        IoUtil.delete(new File(path), true);
        System.out.println("queue file " + path);
        String pretouch = System.getProperty("pretouch");
        Boolean rb = Boolean.getBoolean("rb");
        System.out.println("pretouch: " + pretouch + " RB: " + rb);
        SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder.binary(new File(path));
        builder.blockSize((int) Long.getLong("block.size", builder.blockSize()).longValue());
        builder.rollCycle(RollCycles.LARGE_HOURLY_XSPARSE);
        builder.bufferCapacity(Integer.getInteger("buffer", 32 << 10));
        if (Objects.equals(pretouch, "process"))
            builder.enablePreloader(100);
        if (rb) {
            builder.readBufferMode(BufferMode.Asynchronous);
            builder.writeBufferMode(BufferMode.Asynchronous);
        }
        SingleChronicleQueue queue = builder.build();
        tailer = queue.createTailer();
        appender = queue.acquireAppender();
        bytes = Bytes.elasticByteBuffer(128);
        bytes.writeLong(Long.MIN_VALUE).write(payload.getBytes());
        consumerThread.start();
        if (Objects.equals(pretouch, "thread")) {
            Thread thread = new Thread(() -> {
                ExcerptAppender appender = queue.acquireAppender();
                while (! Thread.currentThread().isInterrupted()) {
                    Jvm.pause(100);
                    appender.pretouch();
                }
            });
            thread.setName("pretouch");
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void test(long startTimeNS) {
        try (DocumentContext dc = appender.writingDocument()) {
            bytes.writeLong(0, startTimeNS);
            dc.wire().bytes().write(bytes);
        }
    }

    @Override
    protected void read(long startReadTime) {
        try (DocumentContext dc = tailer.readingDocument()) {
            if (dc.isPresent()) {
                long sentTime = dc.wire().bytes().readLong();
                consumer.accept(startReadTime, sentTime);
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        tailer.queue().close();
    }
}
