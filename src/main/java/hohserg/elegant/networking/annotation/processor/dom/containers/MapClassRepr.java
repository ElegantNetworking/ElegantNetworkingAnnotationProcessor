package hohserg.elegant.networking.annotation.processor.dom.containers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;

@Value
public class MapClassRepr implements ClassRepr {
    String name;
    String concreteBuilder;
    String concreteFinalizer;
    ClassRepr keyType, valueType;
    Set<Modifier> modifiers;
    TypeMirror original;

    @Override
    public String getSimpleName() {
        return name.substring(name.lastIndexOf('.') + 1) + "_" + keyType.getSimpleName() + "_" + valueType.getSimpleName();
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of(keyType, valueType);
    }

    static Map<String, Pair<String, String>> specials = ImmutableMap.of(
            java.util.Map.class.getName(), Pair.of("java.util.HashMap value = new java.util.HashMap()", "value"),
            java.util.HashMap.class.getName(), Pair.of("java.util.HashMap value = new java.util.HashMap()", "value"),
            ImmutableMap.class.getName(), Pair.of("ImmutableMap.Builder value = ImmutableMap.builder()", "value.build()")
    );

    public static Optional<ClassRepr> prepare(TypeMirror type) {
        Optional<String> maybeSpecial = specials.keySet().stream().filter(type.toString()::startsWith).findAny();
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
        return maybeSpecial
                .map(name1 -> new MapClassRepr(
                        name1,
                        specials.get(name1).getLeft(),
                        specials.get(name1).getRight(),
                        ClassRepr.typeRepresentation(typeArguments.get(0)), ClassRepr.typeRepresentation(typeArguments.get(1)),
                        typeUtils.asElement(type).getModifiers(),
                        type));
    }
}
