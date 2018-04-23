package com.bigpaws.agrona;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 23/04/18.
 */
public class AgronaManyToOneMemoryTest extends AbstractAgronaTest {
    public AgronaManyToOneMemoryTest(LongConsumer longConsumer, String s) throws IOException {
        super(longConsumer, s);
    }

    @Override
    protected RingBuffer createRingBuffer() throws IOException {
        UnsafeBuffer ub = new UnsafeBuffer(new ExpandableArrayBuffer());
        return new ManyToOneRingBuffer(ub);
    }
}
