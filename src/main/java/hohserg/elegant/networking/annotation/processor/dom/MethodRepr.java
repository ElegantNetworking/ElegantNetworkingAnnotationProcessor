package hohserg.elegant.networking.annotation.processor.dom;

import lombok.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

@Value
public class MethodRepr {
    ExecutableElement element;
    String name;
    TypeMirror resultType;
    List<? extends TypeMirror> arguments;
    Set<Modifier> modifiers;

    public static MethodRepr prepare(ExecutableElement element) {
        return new MethodRepr(element, element.getSimpleName().toString(), element.getReturnType(), ((ExecutableType) element.asType()).getParameterTypes(), element.getModifiers());
    }
}
