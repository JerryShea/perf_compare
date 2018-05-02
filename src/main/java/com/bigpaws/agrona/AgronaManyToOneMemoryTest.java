package com.bigpaws.agrona;

import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import java.io.IOException;

/**
 * Created by Jerry Shea on 23/04/18.
 */
public class AgronaManyToOneMemoryTest extends AbstractAgronaTest {
    public AgronaManyToOneMemoryTest(TwoLongConsumer longConsumer, String s) throws IOException {
        super(longConsumer, s);
    }

    @Override
    protected RingBuffer createRingBuffer() throws IOException {
        UnsafeBuffer ub = new UnsafeBuffer(new ExpandableDirectByteBuffer((int) SIZE));
        return new ManyToOneRingBuffer(ub);
    }
}
