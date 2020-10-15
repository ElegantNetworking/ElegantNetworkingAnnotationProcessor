package hohserg.elegant.networking.annotation.processor.dom;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.MethodRequirement;
import lombok.Value;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;

@Value
public class CollectionClassRepr implements ClassRepr {
    String name;
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

    static Set<String> specials = ImmutableSet.of(
            java.util.Set.class.getName(),
            java.util.List.class.getName(),
            java.util.Collection.class.getName()
    );

    static Optional<ClassRepr> prepare(TypeMirror type) {
        Optional<String> maybeSpecial = specials.stream().filter(type.toString()::startsWith).findAny();
        return maybeSpecial
                .map(name1 -> new CollectionClassRepr(
                        name1,
                        ClassRepr.typeRepresentation(((DeclaredType) type).getTypeArguments().get(0)),
                        typeUtils.asElement(type).getModifiers(),
                        type));
    }
}
