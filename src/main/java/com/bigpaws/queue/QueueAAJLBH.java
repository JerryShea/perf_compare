package com.bigpaws.queue;

import com.bigpaws.BaseJLBH;

import java.io.IOException;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class QueueAAJLBH extends BaseJLBH
{
    public QueueAAJLBH() throws IOException {
        super((longConsumer, s) -> new QueueTest(longConsumer, s));
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("rb", "true");
        BaseJLBH.run(new QueueAAJLBH());
    }
}
