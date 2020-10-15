package hohserg.elegant.networking.annotation.processor.dom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Wither;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value
@Wither
public class FieldRepr {
    VariableElement element;
    List<? extends AnnotationMirror> annotations;
    String name;
    TypeMirror originalType;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    ClassRepr type = ClassRepr.typeRepresentation(originalType);
    Set<Modifier> modifiers;


    public static FieldRepr prepare(VariableElement element) {
        return new FieldRepr(element, element.getAnnotationMirrors(), element.toString(), element.asType(), element.getModifiers());
    }
}
