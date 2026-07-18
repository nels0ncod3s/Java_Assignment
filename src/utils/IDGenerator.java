package utils;

import java.util.concurrent.atomic.AtomicInteger;

/** Hands out simple, unique IDs like "BK-0001", "MG-0002", "JN-0003". */
public class IDGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);

    public static String nextId(String prefix) {
        int n = counter.getAndIncrement();
        return String.format("%s-%04d", prefix, n);
    }
}
