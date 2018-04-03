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

    protected AbstractInvoke(final LongConsumer consumer, String payload) {
        consumerThread = new Thread(() -> {
            AffinityLock lock = Affinity.acquireLock();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    read(consumer);
                    // pauser.pause
                }
            } finally {
                lock.release();
            }
        });
        consumerThread.setDaemon(true);
        this.payload = payload;
    }

    @Override
    public void close() throws Exception {
        consumerThread.interrupt();
        Thread.sleep(10);
    }

    protected abstract void read(LongConsumer consumer);
}
