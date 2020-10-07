package hohserg.elegant.networking.annotation.processor.dom;

import lombok.Value;
import lombok.experimental.Wither;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;

@Value
@Wither
public class FieldRepr {
    VariableElement element;
    List<? extends AnnotationMirror> annotations;
    String name;
    TypeMirror type;
    Set<Modifier> modifiers;

    public ClassRepr typeRepresentation(Types typeUtils) {
        return ClassRepr.prepare((TypeElement) typeUtils.asElement(type));
    }


    public static FieldRepr prepare(VariableElement element) {
        return new FieldRepr(element, element.getAnnotationMirrors(), element.toString(), element.asType(), element.getModifiers());
    }
}
