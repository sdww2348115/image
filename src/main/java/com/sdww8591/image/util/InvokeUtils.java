package com.sdww8591.image.util;

import java.util.function.Consumer;

public class InvokeUtils {

    public static <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
        return (arg) -> {
            try {
                consumer.accept(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
