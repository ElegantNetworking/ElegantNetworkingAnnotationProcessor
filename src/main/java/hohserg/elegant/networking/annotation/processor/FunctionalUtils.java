package hohserg.elegant.networking.annotation.processor;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.function.Function;

public interface FunctionalUtils {
    default <A, B, R> Function<Map.Entry<A, R>, Map.Entry<B, R>> leftMapper(Function<A, B> f) {
        return e -> Pair.of(f.apply(e.getKey()), e.getValue());
    }

    default <L, A, B> Function<Map.Entry<L, A>, Map.Entry<L, B>> rightMapper(Function<A, B> f) {
        return e -> Pair.of(e.getKey(), f.apply(e.getValue()));
    }
}
