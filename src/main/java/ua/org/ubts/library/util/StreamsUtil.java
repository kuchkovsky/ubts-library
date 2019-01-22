package ua.org.ubts.library.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class StreamsUtil {

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> ke) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(ke, Boolean.TRUE) == null;
    }

}
