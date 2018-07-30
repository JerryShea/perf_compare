package com.bigpaws.queue;

import com.bigpaws.BaseJLBH;

import java.io.IOException;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public class QueueTmpFSJLBH extends BaseJLBH
{
    public QueueTmpFSJLBH() throws IOException {
        super((longConsumer, s) -> new QueueTest(longConsumer, s, "/tmp"));
    }

    public static void main(String[] args) throws IOException {
        BaseJLBH.run(new QueueTmpFSJLBH());
    }
}
