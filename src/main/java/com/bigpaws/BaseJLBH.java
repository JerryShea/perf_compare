package com.bigpaws;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class BaseJLBH implements JLBHTask
{
    private static JLBH jlbh;
    private final Invoke invoker;

    public BaseJLBH(BiFunctionThrows<LongConsumer, String, Invoke, IOException> createInvoker) throws IOException {
        this.invoker = createInvoker.apply(this::longConsumer, Invoke.PAYLOAD);
    }

    public static void run(JLBHTask task) {
        JLBHOptions options = new JLBHOptions().
                accountForCoordinatedOmmission(true).
                warmUpIterations(20_000).
                iterations(Integer.getInteger("iterations", 100_000)).
                throughput(Integer.getInteger("throughput", 10_000)).
                runs(Integer.getInteger("runs", 3)).
                jlbhTask(task);
        jlbh = new JLBH(options);
        jlbh.start();
    }

    @Override
    public void init(JLBH jlbh) {
    }

    @Override
    public void complete() {
        try {
            invoker.close();
        } catch (Exception e) {
            Jvm.rethrow(e);
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
