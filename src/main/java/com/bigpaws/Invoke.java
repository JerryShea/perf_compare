package com.bigpaws;

/**
 * Created by Jerry Shea on 3/04/18.
 */
public interface Invoke extends AutoCloseable {
    String PAYLOAD = "Lorem ipsum dolor sit";
    String PAYLOAD2 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna ";
    int INTEGER_LENGTH = 4, LONG_LENGTH = 8;

    void test(long startTimeNS);
}
