package com.bigpaws.agrona;

import com.bigpaws.BaseJLBH;

import java.io.IOException;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class AgronaJLBH extends BaseJLBH
{
    public AgronaJLBH() throws IOException {
        super((longConsumer, s) -> new AgronaTest(longConsumer, s));
    }

    public static void main(String[] args) throws IOException {
        BaseJLBH.run(new AgronaJLBH());
    }
}
