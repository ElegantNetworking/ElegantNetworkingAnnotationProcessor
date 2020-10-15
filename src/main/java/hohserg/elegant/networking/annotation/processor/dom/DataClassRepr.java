package hohserg.elegant.networking.annotation.processor.dom;

import lombok.Value;
import lombok.experimental.Wither;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static hohserg.elegant.networking.annotation.processor.LombokUtils.correctFieldModifiers;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Value
@Wither
public class DataClassRepr implements ClassRepr {
    TypeElement element;
    List<? extends AnnotationMirror> annotations;
    String name;
    String simpleName;
    Set<Modifier> modifiers;
    List<FieldRepr> fields;
    List<MethodRepr> methods;
    List<MethodRepr> constructors;

    public static DataClassRepr prepare(TypeElement typeElement) {
        return correctFieldModifiers(new DataClassRepr(
                typeElement,
                typeElement.getAnnotationMirrors(),
                typeElement.getQualifiedName().toString(),
                typeElement.getSimpleName().toString(),
                typeElement.getModifiers(),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD).map(e -> ((VariableElement) e)).map(FieldRepr::prepare).collect(toList()),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList()),
                typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.CONSTRUCTOR).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList())
        ));
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return fields.stream().map(FieldRepr::getType).collect(toSet());
    }

    @Override
    public TypeMirror getOriginal() {
        return element.asType();
    }
}
