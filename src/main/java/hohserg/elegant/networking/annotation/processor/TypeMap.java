package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.code.generator.TypeUtils;
import lombok.AllArgsConstructor;

import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class TypeMap<V> implements Map<TypeMirror, V>, FunctionalUtils {
    Map<TypeMirrorWrapper, V> back = new HashMap<>();

    @AllArgsConstructor
    private static class TypeMirrorWrapper implements TypeUtils {
        public final TypeMirror typeMirror;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeMirrorWrapper)
                return typeEquals(((TypeMirrorWrapper) obj).typeMirror, typeMirror);
            else
                return false;
        }

        @Override
        public int hashCode() {
            return uniqueHash(typeMirror);
        }
    }

    @Override
    public int size() {
        return back.size();
    }

    @Override
    public boolean isEmpty() {
        return back.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return back.containsKey(new TypeMirrorWrapper((TypeMirror) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return back.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return back.get(new TypeMirrorWrapper((TypeMirror) key));
    }

    @Override
    public V put(TypeMirror key, V value) {
        return back.put(new TypeMirrorWrapper(key), value);
    }

    @Override
    public V remove(Object key) {
        return back.remove(new TypeMirrorWrapper((TypeMirror) key));
    }

    @Override
    public void putAll(Map<? extends TypeMirror, ? extends V> m) {
        if (m instanceof TypeMap)
            back.putAll(((TypeMap) m).back);
        else
            m.forEach(this::put);
    }

    @Override
    public void clear() {
        back.clear();
    }

    @Override
    public Set<TypeMirror> keySet() {
        return back.keySet().stream().map(k -> k.typeMirror).collect(toSet());
    }

    @Override
    public Collection<V> values() {
        return back.values();
    }

    @Override
    public Set<Entry<TypeMirror, V>> entrySet() {
        return back.entrySet().stream().map(leftMapper(k -> k.typeMirror)).collect(toSet());
    }
}
