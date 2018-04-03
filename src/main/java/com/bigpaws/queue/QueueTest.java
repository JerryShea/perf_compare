package com.bigpaws.queue;

import com.bigpaws.AbstractInvoke;
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
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
@State(Scope.Benchmark)
public class QueueTest extends AbstractInvoke {
    private static final String PATH = IoUtil.tmpDirName() + "/queue";
    private final ExcerptTailer tailer;
    private final ExcerptAppender appender;

    QueueTest(LongConsumer consumer, String payload) {
        super(consumer, payload);
        IoUtil.delete(new File(PATH), true);
        final SingleChronicleQueue queue = SingleChronicleQueueBuilder.binary(PATH).rollCycle(RollCycles.LARGE_HOURLY_XSPARSE).build();
        tailer = queue.createTailer();
        appender = queue.acquireAppender();
        consumerThread.start();
    }

    @Override
    public void test(long startTimeNS) {
        try (DocumentContext dc = appender.writingDocument()) {
            dc.wire().bytes().writeLong(startTimeNS).write8bit(payload);
        }
    }

    @Override
    protected void read(LongConsumer consumer) {
        try (DocumentContext dc = tailer.readingDocument()) {
            if (dc.isPresent()) {
                long sentTime = dc.wire().bytes().readLong();
                consumer.accept(sentTime);
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        tailer.queue().close();
    }
}
