package com.bigpaws;

/**
 * Created by Jerry Shea on 11/04/18.
 */
public interface BiFunctionThrows<T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;
}
