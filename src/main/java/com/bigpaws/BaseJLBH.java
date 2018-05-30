package com.bigpaws;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;
import net.openhft.chronicle.core.util.NanoSampler;

import java.io.IOException;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class BaseJLBH implements JLBHTask
{
    private static JLBH jlbh;
    private final Invoke invoker;
    private NanoSampler writerProbe;
    private NanoSampler readerProbe;
    private volatile long startTimeNs;

    public BaseJLBH(BiFunctionThrows<Invoke.TwoLongConsumer, String, Invoke, IOException> createInvoker) throws IOException {
        this.invoker = createInvoker.apply(this::longConsumer, Boolean.getBoolean("payload2") ? Invoke.PAYLOAD2 : Invoke.PAYLOAD);
    }

    public static void run(JLBHTask task) {
        JLBHOptions options = new JLBHOptions().
                accountForCoordinatedOmmission(true).
                warmUpIterations(100_000).
                iterations(Integer.getInteger("iterations", 100_000)).
                throughput(Integer.getInteger("throughput", 10_000)).
                runs(Integer.getInteger("runs", 3)).
                recordOSJitter(Boolean.getBoolean("record.os.jitter")).
                jlbhTask(task);
        jlbh = new JLBH(options);
        jlbh.start();
    }

    @Override
    public void init(JLBH jlbh) {
        writerProbe = jlbh.addProbe("writer");
        readerProbe = jlbh.addProbe("reader");
    }

    @Override
    public void warmedUp() {
        Monitor.enabled = true;
    }

    @Override
    public void complete() {
        try {
            Monitor.enabled = false;
            invoker.close();
        } catch (Exception e) {
            Jvm.rethrow(e);
        }
    }

    protected void longConsumer(long startReadTime, long sentTime) {
        long now = System.nanoTime();
        jlbh.sampleNanos(now - sentTime);
        readerProbe.sampleNanos(now - startReadTime);
    }

    @Override
    public void run(long startTimeNS) {
        this.startTimeNs = startTimeNS;
        invoker.test(startTimeNS);
        writerProbe.sampleNanos(System.nanoTime() - startTimeNS);
        this.startTimeNs = Long.MIN_VALUE;
    }

    public long startTimeNS() {
        return this.startTimeNs;
    }
}
