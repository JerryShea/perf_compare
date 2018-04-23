package com.bigpaws.agrona;

import com.bigpaws.BaseJLBH;

import java.io.IOException;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class AgronaManyToOneMemoryJLBH extends BaseJLBH
{
    public AgronaManyToOneMemoryJLBH() throws IOException {
        super((longConsumer, s) -> new AgronaManyToOneMemoryTest(longConsumer, s));
    }

    public static void main(String[] args) throws IOException {
        BaseJLBH.run(new AgronaManyToOneMemoryJLBH());
    }
}
