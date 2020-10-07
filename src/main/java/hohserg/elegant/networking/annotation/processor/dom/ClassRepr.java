package hohserg.elegant.networking.annotation.processor.dom;

import lombok.Value;
import lombok.experimental.Wither;

import javax.lang.model.element.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
@Wither
public class ClassRepr {
    TypeElement element;
    List<? extends AnnotationMirror> annotations;
    String name;
    List<FieldRepr> fields;
    List<MethodRepr> methods;
    List<MethodRepr> constructors;

    public static ClassRepr prepare(TypeElement typeElement) {
        return new ClassRepr(
                typeElement,
                typeElement.getAnnotationMirrors(),
                typeElement.getQualifiedName().toString(),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD).map(e -> ((VariableElement) e)).map(FieldRepr::prepare).collect(toList()),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList()),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.CONSTRUCTOR).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList())
        );
    }
}
