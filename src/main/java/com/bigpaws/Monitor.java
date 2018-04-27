package com.bigpaws;

import net.openhft.chronicle.core.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Created by Jerry Shea on 24/04/18.
 */
public class Monitor extends Thread {
    static final long TIMING_MONITOR_LIMIT_NS = Integer.getInteger("monitor.limit.ns", 1_000_000);
    static final int TIMING_MONITOR_DELAY_SECS = Integer.getInteger("monitor.delay.secs", 2);
    public static volatile boolean enabled = false;
    private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);
    private final Thread thread;
    private final LongSupplier startTimeNs;

    public Monitor(Thread thread, LongSupplier startTimeNS) {
        this.thread = thread;
        this.startTimeNs = startTimeNS;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        Jvm.pause(TimeUnit.SECONDS.toMillis(TIMING_MONITOR_DELAY_SECS));
        while (! enabled)
            Jvm.pause(1);
        while (thread.isAlive() && enabled) {
            long time = startTimeNs.getAsLong();
            if (time != Long.MIN_VALUE) {
                long latency = System.nanoTime() - time;
                if (latency > TIMING_MONITOR_LIMIT_NS) {
                    StringBuilder out = new StringBuilder().append("THIS IS NOT AN ERROR, but a profile of the thread, \"").append(thread.getName()).append("\" blocked for ").append(latency / 1000000).append(" ms. ");
                    Jvm.trimStackTrace(out, thread.getStackTrace());
                    String outString = out.toString();
                    if (! outString.contains("net.openhft.chronicle.core.jlbh.JLBH.end"))
                        LOG.info(outString);
                }
            }
            Jvm.pause(1);
        }
    }
}
