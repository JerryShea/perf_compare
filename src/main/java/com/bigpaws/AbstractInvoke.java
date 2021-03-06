package com.bigpaws;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public abstract class AbstractInvoke implements Invoke {
    public static final int BASE_BUFFER_SIZE = 32 << 20;
    protected final Thread consumerThread;
    protected final String payload;
    protected final TwoLongConsumer consumer;

    protected AbstractInvoke(final TwoLongConsumer consumer, final String payload) {
        consumerThread = new ConsumerThread();
        this.payload = payload;
        this.consumer = consumer;
    }

    @Override
    public void close() throws Exception {
        consumerThread.interrupt();
        Thread.sleep(10);
    }

    protected abstract void read(long readStartTime);

    private class ConsumerThread extends Thread {
        private volatile long startTimeNs;

        ConsumerThread() {
            this.setDaemon(true);
            this.setName(this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            AffinityLock lock = Affinity.acquireLock();
            new Monitor(Thread.currentThread(), this::startTimeNS).start();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    startTimeNs = System.nanoTime();
                    read(startTimeNs);
                    startTimeNs = Long.MIN_VALUE;
                }
            } finally {
                lock.release();
            }
        }

        private long startTimeNS() {
            return startTimeNs;
        }
    }
}
