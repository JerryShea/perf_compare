package com.bigpaws;

import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public abstract class AbstractJLBH implements JLBHTask
{
    private static JLBH jlbh;
    private Invoke invoker;

    public static void run(AbstractJLBH impl) {
        JLBHOptions options = new JLBHOptions().
                accountForCoordinatedOmmission(true).
                warmUpIterations(20_000).
                iterations(Integer.getInteger("iterations", 100_000)).
                throughput(Integer.getInteger("throughput", 10_000)).
                runs(Integer.getInteger("runs", 3)).
                jlbhTask(impl);
        jlbh = new JLBH(options);
        jlbh.start();
    }

    protected abstract Invoke createInvoker(LongConsumer longConsumer, String payload) throws IOException;

    @Override
    public void init(JLBH jlbh) {
        try {
            invoker = createInvoker(this::longConsumer, Invoke.PAYLOAD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void complete() {
        try {
            invoker.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void longConsumer(long sentTime) {
        jlbh.sampleNanos(System.nanoTime() - sentTime);
    }

    @Override
    public void run(long startTimeNS) {
        invoker.test(startTimeNS);
    }
}
