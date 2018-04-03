package com.bigpaws.agrona;

import com.bigpaws.AbstractJLBH;
import com.bigpaws.Invoke;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class AgronaJLBH extends AbstractJLBH
{
    public static void main(String[] args) throws IOException {
        AbstractJLBH.run(new AgronaJLBH());
    }

    @Override
    protected Invoke createInvoker(LongConsumer longConsumer, String payload) throws IOException {
        return new AgronaTest(this::longConsumer, Invoke.PAYLOAD);
    }
}
