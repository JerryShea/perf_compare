package com.bigpaws.queue;

import com.bigpaws.AbstractJLBH;
import com.bigpaws.Invoke;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class QueueJLBH extends AbstractJLBH
{
    public static void main(String[] args) throws IOException {
        AbstractJLBH.run(new QueueJLBH());
    }

    @Override
    protected Invoke createInvoker(LongConsumer longConsumer, String payload) throws IOException {
        return new QueueTest(this::longConsumer, Invoke.PAYLOAD);
    }
}
