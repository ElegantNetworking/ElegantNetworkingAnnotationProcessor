package hohserg.elegant.networking.annotation.processor.dom.containers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.note;
import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;

@Value
public class CollectionClassRepr implements ClassRepr {
    String name;
    String concreteBuilder;
    String concreteFinalizer;
    ClassRepr elementType;
    Set<Modifier> modifiers;
    TypeMirror original;

    @Override
    public String getSimpleName() {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of(elementType);
    }

    static Map<String, Pair<String, String>> specials = ImmutableMap.of(
            java.util.Set.class.getName(), Pair.of("java.util.HashSet value = new java.util.HashSet()", "value"),
            java.util.List.class.getName(), Pair.of("java.util.ArrayList value = new java.util.ArrayList()", "value"),
            java.util.Collection.class.getName(), Pair.of("java.util.ArrayList value = new java.util.ArrayList()", "value"),
            ImmutableList.class.getName(), Pair.of("ImmutableList.Builder value = ImmutableList.builder()", "value.build()")//,
            //`???` , Pair.of("Object[] value = new Object[size]", "value")
    );

    public static Optional<ClassRepr> prepare(TypeMirror type) {
        Optional<String> maybeSpecial = specials.keySet().stream().filter(type.toString()::startsWith).findAny();
        return maybeSpecial
                .map(name1 -> new CollectionClassRepr(
                        name1,
                        specials.get(name1).getLeft(),
                        specials.get(name1).getRight(),
                        ClassRepr.typeRepresentation(((DeclaredType) type).getTypeArguments().get(0)),
                        typeUtils.asElement(type).getModifiers(),
                        type));
    }
}
