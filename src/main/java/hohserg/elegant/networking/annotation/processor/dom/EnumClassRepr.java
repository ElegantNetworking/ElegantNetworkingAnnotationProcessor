package hohserg.elegant.networking.annotation.processor.dom;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.MethodRequirement;
import lombok.Value;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.elementUtils;
import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;

@Value
public class EnumClassRepr implements ClassRepr {
    TypeElement element;
    String name;
    String simpleName;
    Set<Modifier> modifiers;

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of();
    }

    @Override
    public TypeMirror getOriginal() {
        return element.asType();
    }

    public static Optional<ClassRepr> prepare(TypeMirror type) {
        TypeMirror enumBaseType = typeUtils.getDeclaredType(elementUtils.getTypeElement(Enum.class.getName()), type);
        if (typeUtils.isSubtype(type, enumBaseType)) {
            TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
            return Optional.of(new EnumClassRepr(typeElement, typeElement.getQualifiedName().toString(), typeElement.getSimpleName().toString(), typeElement.getModifiers()));
        } else
            return Optional.empty();
    }

    @Override
    public Stream<MethodRequirement> getRequirementMethods() {
        return Stream.of(new MethodRequirement.EnumMethod(this));
    }
}
