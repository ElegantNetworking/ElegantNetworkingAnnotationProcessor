package hohserg.elegant.networking.annotation.processor.dom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
public class MethodRepr {
    ExecutableElement element;
    String name;
    TypeMirror originalResultType;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    ClassRepr resultType = ClassRepr.typeRepresentation(originalResultType);
    List<? extends TypeMirror> originalArguments;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    List<ClassRepr> arguments = originalArguments.stream().map(ClassRepr::typeRepresentation).collect(Collectors.toList());
    Set<Modifier> modifiers;

    public static MethodRepr prepare(ExecutableElement element) {
        return new MethodRepr(
                element,
                element.getSimpleName().toString(),
                element.getReturnType(),
                ((ExecutableType) element.asType()).getParameterTypes(),
                element.getModifiers());
    }
}
