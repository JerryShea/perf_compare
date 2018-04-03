package com.bigpaws;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public abstract class AbstractInvoke implements Invoke {
    protected final Thread consumerThread;
    protected final String payload;
    protected final LongConsumer consumer;

    protected AbstractInvoke(final LongConsumer consumer, String payload) {
        consumerThread = new Thread(() -> {
            AffinityLock lock = Affinity.acquireLock();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    read();
                    // pauser.pause
                }
            } finally {
                lock.release();
            }
        });
        consumerThread.setDaemon(true);
        this.payload = payload;
        this.consumer = consumer;
    }

    @Override
    public void close() throws Exception {
        consumerThread.interrupt();
        Thread.sleep(10);
    }

    protected abstract void read();
}
